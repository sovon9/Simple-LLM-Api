# Simple-LLM-Api — Spring AI Tool Calling, Explained From Scratch

This project is a minimal Spring Boot service that talks to an LLM (Google
Gemini, via `spring-ai-starter-model-google-genai`) using **Spring AI** and
demonstrates **tool calling** (a.k.a. *function calling*): giving the model
the ability to invoke real Java methods in your application instead of just
generating text.

This README has two goals:

1. Teach you **what tool calling is and how it works**, generically, so you
   can add it to *any* Spring Boot server, with *any* Spring-AI-supported
   model provider (OpenAI, Azure OpenAI, Anthropic, Ollama, Google GenAI,
   Mistral, etc.).
2. Walk through **this repo's actual code**, line by line, as a concrete,
   working reference implementation.

No prior experience with Spring AI is assumed.

---

## Table of contents

1. [What problem does tool calling solve?](#1-what-problem-does-tool-calling-solve)
2. [The mental model: what actually happens on the wire](#2-the-mental-model-what-actually-happens-on-the-wire)
3. [Adding tool calling to *any* Spring Boot server — step by step](#3-adding-tool-calling-to-any-spring-boot-server--step-by-step)
4. [Deep dive: the `@Tool` and `@ToolParam` annotations](#4-deep-dive-the-tool-and-toolparam-annotations)
5. [Passing hidden data to a tool: `ToolContext`](#5-passing-hidden-data-to-a-tool-toolcontext)
6. [Registering tools: globally vs. per-request](#6-registering-tools-globally-vs-per-request)
7. [Multiple tools & how the model picks one](#7-multiple-tools--how-the-model-picks-one)
8. [Error handling inside a tool](#8-error-handling-inside-a-tool)
9. [Internal vs. external tool execution (advanced)](#9-internal-vs-external-tool-execution-advanced)
10. [Walkthrough of this repository's code](#10-walkthrough-of-this-repositorys-code)
11. [Running and testing the API](#11-running-and-testing-the-api)
12. [Common pitfalls](#12-common-pitfalls)
13. [Swapping to a different LLM provider](#13-swapping-to-a-different-llm-provider)
14. [Further reading](#14-further-reading)

---

## 1. What problem does tool calling solve?

An LLM, by itself, can only do one thing: turn text into more text. It has
no access to your database, your clock, your file system, or any API you
own. **Tool calling** closes that gap:

- You describe a Java method to the model ("here's a function called
  `getStockTrendAdvice`, it takes no arguments, and returns investment
  advice").
- When the model decides that calling that function would help answer the
  user's question, it doesn't call it itself — it can't, it has no network
  access to your JVM. Instead, it replies with a structured message saying,
  in effect, *"please call `getStockTrendAdvice()` and tell me what it
  returns."*
- **Spring AI intercepts that reply**, finds the matching Java method on a
  Spring bean, invokes it for you, takes the return value, and sends it back
  to the model as a new message.
- The model then uses that real data to produce its final, natural-language
  answer.

This is how a chatbot can answer "what's the weather in my city" or "move
$50 from savings to checking" — the LLM is the reasoning/orchestration layer,
your code is the thing that actually *does* things and knows *real,
current* facts.

Spring AI's contribution is that **you only write a plain Java method**.
Spring AI handles:
- Generating the JSON Schema describing the method so the model understands
  its signature.
- Parsing the model's tool-call request (which arrives as JSON) into real
  Java method arguments.
- Invoking the method via reflection.
- Serializing the return value and feeding it back to the model.
- Looping this whole exchange until the model stops asking for tools and
  gives a final answer.

You never manually parse JSON or write an if/else dispatcher for "which tool
did the model ask for" — Spring AI's `ChatClient` does all of that for you.

---

## 2. The mental model: what actually happens on the wire

Here is the full round trip for a single user message, when tool calling is
involved (this is what `ChatClient` does internally — "internal tool
execution", the default mode):

```
┌──────────┐          ┌────────────┐          ┌───────────────┐          ┌─────────┐
│  Your     │  prompt  │  ChatClient │  HTTP    │   LLM provider │          │  Your   │
│ Controller│ ───────> │ (Spring AI) │ ───────> │ (Gemini/OpenAI)│          │  Tool   │
└──────────┘          └────────────┘          └───────┬────────┘          └─────────┘
                                                         │
                                          "I want to call tool X(args)"
                                                         │
                                                         v
                                               ┌────────────────┐
                                               │   ChatClient    │
                                               │ looks up method │
                                               │ annotated with   │
                                               │ @Tool(name="X")  │
                                               └───────┬────────┘
                                                         │ reflection call
                                                         v
                                               ┌────────────────┐
                                               │  Your Tool bean │
                                               │  X(args) runs   │
                                               │  returns result │
                                               └───────┬────────┘
                                                         │ result serialized to JSON
                                                         v
                                        sends tool result back to the LLM
                                                         │
                                                         v
                                          LLM produces final natural-language
                                                answer using that result
                                                         │
                                                         v
                                               returned to your Controller
```

Key point: **the HTTP round trip to the LLM may happen more than once** for
a single incoming user request, because the model first asks for a tool
call, then — after seeing the tool's result — produces the real answer.
Spring AI's `ChatClient.call()` hides this multi-step loop behind a single
blocking method call.

---

## 3. Adding tool calling to *any* Spring Boot server — step by step

This section is provider-agnostic. Substitute whichever Spring AI "starter"
matches your LLM provider.

### 3.1. Add dependencies

In `pom.xml`, import the Spring AI BOM (Bill Of Materials) so all Spring AI
artifacts share a consistent version, then add the starter for your chosen
model provider:

```xml
<properties>
    <java.version>17</java.version>
    <spring-ai.version>2.0.0</spring-ai.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>

    <!-- Pick ONE model starter depending on your provider: -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-openai</artifactId>
        <!-- or spring-ai-starter-model-google-genai, spring-ai-starter-model-azure-openai,
             spring-ai-starter-model-anthropic, spring-ai-starter-model-ollama, etc. -->
    </dependency>
</dependencies>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>${spring-ai.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

The **tool-calling mechanism itself (`@Tool`, `ChatClient.defaultTools()`,
`ToolContext`, etc.) is identical across every provider** — it lives in
`spring-ai-model` / `spring-ai-client-chat`, which every starter pulls in
transitively. This is the whole point of Spring AI's abstraction: you write
your tool once, and it works whether the model behind it is Gemini, GPT, or
Claude (model-provider support for tool calling varies in capability, but
the Spring AI programming model doesn't change).

### 3.2. Configure provider credentials

In `application.properties` (or `application.yml`):

```properties
# Example for Google GenAI (Gemini) — adjust property names per provider
spring.ai.google.genai.api-key=${GEMINI_API_KEY}
spring.ai.google.genai.chat.model=gemini-3.5-flash-lite
```

Never hardcode API keys. Use an environment variable (`${GEMINI_API_KEY}`
above reads it from the environment) or a secrets manager.

### 3.3. Write your tool as a plain method on a Spring bean

```java
package com.example.myapp.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component   // must be a Spring-managed bean so it can be injected and registered
public class WeatherTool {

    @Tool(
        name = "get_current_weather",                     // the name the MODEL sees and uses to request this tool
        description = "Returns the current weather for a given city name"  // tells the model WHEN to use it
    )
    public String getCurrentWeather(
            @ToolParam(description = "The city to look up, e.g. 'London'") String city) {

        // ... call a real weather API, a database, whatever you like ...
        return "It is 18°C and cloudy in " + city;
    }
}
```

That's it — this is a normal Spring `@Component`. The `@Tool` annotation is
the only thing that marks it as callable by the LLM.

### 3.4. Register the tool with a `ChatClient`

```java
package com.example.myapp.config;

import com.example.myapp.tools.WeatherTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, WeatherTool weatherTool) {
        return builder
                .defaultTools(weatherTool)   // <-- registers EVERY @Tool-annotated method on this bean
                .build();
    }
}
```

`ChatClient.Builder` is auto-configured by Spring AI's starter based on
whatever provider you added in step 3.1 — you don't construct it yourself.

### 3.5. Call the `ChatClient` from a controller

```java
package com.example.myapp.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/chat")
    public String chat(@RequestBody String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}
```

That's the entire integration. Ask it `"What's the weather in Paris?"` and
the model will transparently call `getCurrentWeather("Paris")` for you and
weave the result into its answer — no manual JSON parsing, no dispatch
logic, no HTTP handling for the tool call itself.

---

## 4. Deep dive: the `@Tool` and `@ToolParam` annotations

### `@Tool`

```java
@Tool(name = "...", description = "...", returnDirect = false)
```

| Attribute      | Purpose                                                                                                                                                      |
|----------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `name`         | The identifier the model uses to request this tool. Must be unique among all registered tools for a given request. Keep it short, descriptive, machine-like (`get_order_status`, not `GetOrderStatusForUser`). Some providers restrict allowed characters to letters, digits, `_` and `-` — stick to `snake_case` or `lowerCamel` to be safe across providers. |
| `description`  | **This is the most important field.** The model decides *whether and when* to call your tool almost entirely based on this text. Write it like you're explaining to a new teammate exactly what the tool does and when it's useful. Vague descriptions cause the model to never call the tool, or to call it for the wrong reasons. |
| `returnDirect` | If `true`, the tool's raw return value is returned to the *caller* immediately, skipping the step where the model turns it into natural language. Default `false`. |

If you omit `name`, Spring AI derives one from the method name. Omitting
`description` is legal but strongly discouraged — the model has nothing to
go on.

### `@ToolParam`

Put this on each method parameter (other than `ToolContext`, see §5) that
you want the model to fill in:

```java
@Tool(name = "convert_currency", description = "Converts an amount from one currency to another")
public String convertCurrency(
        @ToolParam(description = "Amount to convert, e.g. 100.50") double amount,
        @ToolParam(description = "Three-letter source currency code, e.g. USD") String from,
        @ToolParam(description = "Three-letter target currency code, e.g. EUR") String to) {
    // ...
}
```

Spring AI introspects the method signature (parameter names, types, and
`@ToolParam` descriptions) and auto-generates a **JSON Schema** that is sent
to the model as part of the request, describing exactly what arguments the
tool expects. The model then returns a tool-call request whose arguments
are JSON matching that schema; Spring AI deserializes that JSON back into
real Java objects (`double`, `String`, your own POJOs, `List<T>`, etc.) and
calls your method with them.

- **Supported parameter types:** primitives and their wrappers, `String`,
  `enum`, `List`/`Set`/arrays of the above, and arbitrary POJOs (Spring AI
  will generate a nested schema for their fields too). Avoid parameter types
  Jackson can't deserialize cleanly.
- **Supported return types:** `String` is returned as-is. Any other object
  is serialized to JSON via Jackson before being sent back to the model, so
  it should be a plain data-carrying type (record, POJO, `Map`, `List`,
  etc.).
- `@ToolParam(required = false)` marks an optional parameter.

**Methods must be `public`** — Spring AI invokes them via reflection and
cannot call non-public methods on a proxied/non-opened bean.

---

## 5. Passing hidden data to a tool: `ToolContext`

Sometimes you need to give a tool information that **must not** be exposed
to or filled in by the model — e.g., the currently authenticated user's ID,
a tenant ID, an auth token pulled from the HTTP request. That's what
`ToolContext` is for.

Add a parameter of type `org.springframework.ai.chat.model.ToolContext` to
your tool method — it's excluded from the JSON schema sent to the model, so
the model never sees it and can never set it:

```java
@Tool(name = "trend-investment", description = "tells about the best way to earn in a trend stock market")
public String trendInvestment(ToolContext toolContext) {
    String username = (String) toolContext.getContext().get("username");
    // use username for personalization, auditing, authorization, etc.
    return "...advice...";
}
```

You populate it at call time, from the controller, with whatever your
application already knows (e.g. from a `@RequestHeader`, the
`SecurityContext`, a JWT claim):

```java
chatClient.prompt()
        .user(message)
        .toolContext(Map.of("username", username))   // flows into EVERY tool invoked for this call
        .call()
        .content();
```

Think of `ToolContext` as a side channel from *your controller* to *your
tool*, completely bypassing the LLM. It's the correct place for anything
security- or identity-related — never ask the model to supply a user ID or
auth token as a normal tool parameter, because a malicious or confused
prompt could make the model invent or leak one.

---

## 6. Registering tools: globally vs. per-request

- **`ChatClient.Builder.defaultTools(...)`** (used in `ChatClientConfig` in
  this repo) — registers tools for *every* request made through that
  `ChatClient` bean. Good for tools that should always be available
  (general-purpose assistants).

- **`.tools(...)` on a specific request** — registers tools *only* for that
  one call, on top of (or instead of) the defaults:

  ```java
  chatClient.prompt()
          .user(message)
          .tools(someOtherTool)   // only available for this single request
          .call()
          .content();
  ```

  Useful when different endpoints/use-cases should expose different
  capabilities to the model (e.g., an admin endpoint exposes a
  `deleteUser` tool that a public-facing chat endpoint must never see).

You can pass either **bean instances** (as in this repo —
`.defaultTools(tool)`, which registers every `@Tool`-annotated method found
on that object) or, for cases where you don't control the source class (a
third-party library, a lambda, a static method), build a `ToolCallback`
programmatically with `FunctionToolCallback.builder(...)` and register that
instead. For the vast majority of applications, the `@Tool`-annotated-bean
approach shown throughout this README is all you need.

---

## 7. Multiple tools & how the model picks one

You can register as many tools as you like. On each turn, the model is sent
*all* registered tools' names/descriptions/schemas alongside the
conversation, and it decides — purely from the user's message and the
descriptions you wrote — whether to answer directly or request one (or
several) tool calls. Spring AI then executes whichever tools were
requested and loops until the model is satisfied.

Practical implications:
- **Descriptions are your API contract with the model's judgment.** Two
  tools with overlapping, vague descriptions will cause flaky,
  unpredictable tool selection. Be specific and mutually exclusive.
- **Tool names must be unique** across everything registered for a given
  request (defaults + per-call tools combined).
- A single user message can trigger **multiple tool calls in one round**
  (e.g., "compare the weather in Paris and Tokyo" might call
  `get_current_weather` twice), and Spring AI handles that transparently.

---

## 8. Error handling inside a tool

If your tool method throws an exception, Spring AI does **not** crash the
whole request by default. It catches the exception and feeds a description
of the failure back to the model as the "tool result", so the model can
react gracefully (apologize, retry with different arguments, fall back to a
generic answer) instead of your API returning a raw 500 error.

```java
@Tool(name = "get_order_status", description = "Looks up the shipping status of an order by its order ID")
public String getOrderStatus(@ToolParam(description = "The order ID") String orderId) {
    Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("No order found with ID " + orderId));
    return order.getStatus().toString();
}
```

If `orderId` doesn't exist, the model receives something like "tool
execution failed: No order found with ID ..." and can relay that
sensibly to the user ("I couldn't find an order with that ID — could you
double check it?") rather than the whole HTTP call blowing up.

If you need custom handling (e.g., don't leak internal exception messages),
catch the exception yourself inside the tool method and return a
user-safe string describing the failure instead of letting it propagate.

---

## 9. Internal vs. external tool execution (advanced)

By default, Spring AI uses **internal tool execution**: the entire
request → tool-call → tool-execution → response loop described in §2
happens inside the single `chatClient.prompt()...call()` invocation, and
your controller only ever sees the final text answer.

Some scenarios need **external tool execution** instead — e.g.:
- The tool call must be approved by a human before running (human-in-the-loop).
- The "tool" is actually a client-side action (e.g., a button the *frontend*
  must render), not something your backend can execute at all.
- You want to execute the tool asynchronously or on a different service.

For those cases, disable internal execution on the request's `ChatOptions`
(`ToolCallingChatOptions.builder().internalToolExecutionEnabled(false)...`).
The `call()` then returns the raw tool-call request instead of auto-executing
it, and your code is responsible for executing it and sending the result
back in a follow-up call. This is an advanced/uncommon path — most servers,
including this one, should stick with the default internal execution.

---

## 10. Walkthrough of this repository's code

```
src/main/java/com/sovon9/Simple_LLM_Api/
├── SimpleLlmApiApplication.java      # standard Spring Boot entry point, nothing tool-specific
├── config/ChatClientConfig.java      # builds and configures the ChatClient bean
├── controlller/ChatController.java   # exposes /chat endpoint
└── tools/InvestmentTool.java         # the actual tool the model can call
```

### `tools/InvestmentTool.java`

```java
@Component
public class InvestmentTool {

    Logger LOGGER = LoggerFactory.getLogger(InvestmentTool.class);

    @Tool(name = "trend-investment", description = "tells about the best way to earn in a trent stock market")
    public String trentInvestment(ToolContext toolContext) {
        LOGGER.error("accessing toolcontext data ========> " + toolContext.getContext().get("username"));
        return "always check shares in a multi timeframe and identify the trend using monthly chart and then use that chart to plot" +
                "20MA and 50MA and using that see if the current position of the share is in above those lines or not. If current position is above 20-50MA then" +
                "wait for a pullback and buy it";
    }
}
```

- `@Component` makes this a Spring bean so it can be constructor-injected
  into `ChatClientConfig` (and so it could, in a real app, have its own
  dependencies like a `StockPriceRepository` injected into it).
- `@Tool(name = "trend-investment", ...)` is the only thing that makes this
  method visible to the LLM. Note the `description` has a typo ("trent"
  instead of "trend") — harmless functionally (the model still reads it as
  natural language and gets the gist), but worth tightening up if you reuse
  this as a template, since description quality directly affects how
  reliably the model decides to call the tool (see §4 and §7).
- The method takes **only a `ToolContext`** and no `@ToolParam`-annotated
  arguments — meaning the model cannot pass this tool any data at all; it
  can only decide *whether* to call it. All the "input" here
  (`username`) is injected by the *application*, not the model, via
  `ToolContext` (see §5). This is a legitimate and common pattern for tools
  that are personalized by server-side identity rather than by anything the
  model needs to supply.
- The `LOGGER.error(...)` call is just demonstrating that `username` really
  does flow from the controller into the tool at invocation time — in a
  real app you'd use `LOGGER.debug`/`info` for this, not `error`.
- The return value is a plain `String`, so it's sent back to the model
  verbatim as the tool's result (no JSON serialization needed for `String`
  returns).

### `config/ChatClientConfig.java`

```java
@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory, InvestmentTool tool) {
        MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        return builder
                .defaultTools(tool)
                .defaultAdvisors(memoryAdvisor)
                .build();
    }
}
```

- `ChatClient.Builder` is auto-configured by the
  `spring-ai-starter-model-google-genai` starter based on
  `application.properties`.
- `InvestmentTool tool` is injected by Spring (it's a `@Component`) and
  passed straight into `.defaultTools(tool)` — this registers the
  `trend-investment` tool for **every** request made with this `ChatClient`
  bean, app-wide (see §6).
- `.defaultAdvisors(memoryAdvisor)` is unrelated to tool calling — it wires
  up **chat memory** (conversation history persisted via JDBC, backed by
  MySQL per `application.properties`), so the model remembers prior turns
  of the same conversation. It's included here because the controller uses
  it (`ChatMemory.CONVERSATION_ID`), but it is a separate Spring AI feature
  from tool calling. Mentioned for completeness so the full picture of this
  file makes sense.

### `controlller/ChatController.java`

```java
@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/chat")
    public String chatToLLM(@RequestBody String message, @RequestHeader("username") String username) {
        ChatClient.CallResponseSpec callResponseSpec = chatClient
                .prompt()
                .user(message)
                .toolContext(Map.of("username", username))
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, username))
                .call();
        return callResponseSpec.content();
    }
}
```

- The `InvestmentTool` bean was already wired in via `defaultTools(tool)`
  in the config class — the controller doesn't need to know the tool
  exists at all. It just builds a prompt.
- `.toolContext(Map.of("username", username))` is exactly the mechanism
  from §5: the `username` pulled off the `username` HTTP header is injected
  into `ToolContext`, invisible to the model, and only
  `InvestmentTool.trentInvestment` reads it back out.
- `.advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, username))`
  is the chat-memory feature again — it uses `username` as the conversation
  ID, so memory is scoped per user. Not tool-calling, but worth knowing
  it's a separate parameter from `toolContext`, even though both happen to
  reuse the same `username` value here.
- Note this is mapped with `@GetMapping` but reads a `@RequestBody` —
  technically unusual (GET requests aren't meant to carry a body, and some
  HTTP clients/proxies strip it), but Spring MVC and most HTTP clients
  (`curl`, Postman) will still send/accept it. If you hit issues with some
  client refusing to send a body on GET, switch this to `@PostMapping`.

### `src/main/resources/application.properties`

```properties
spring.application.name=Simple-LLM-Api

spring.ai.google.genai.api-key=${GEMINI_API_KEY}
spring.ai.google.genai.chat.model=gemini-3.5-flash-lite

#DB config
spring.datasource.url=jdbc:mysql://localhost:3308/dev
spring.datasource.username=sovon9
spring.datasource.password=password

spring.jpa.show-sql=true

# to create the table always
spring.ai.chat.memory.repository.jdbc.initialize-schema=always
```

- `spring.ai.google.genai.api-key=${GEMINI_API_KEY}` — reads the Gemini API
  key from an environment variable named `GEMINI_API_KEY`. You must set
  this before running the app (see §11).
- The `spring.datasource.*` and `spring.jpa.show-sql` properties configure
  the MySQL database used by `spring-ai-starter-model-chat-memory-repository-jdbc`
  to persist conversation history — again, this is the chat-memory feature,
  not tool calling, but it must be reachable (MySQL running on
  `localhost:3308`, database `dev` existing) for the app to start
  successfully, since `ChatMemory` is a required constructor argument of
  `chatClient(...)`.
- `spring.ai.chat.memory.repository.jdbc.initialize-schema=always` —
  recreates the chat-memory tables on every startup. Fine for local
  development; you'd typically use `embedded`/conditional modes in
  production so you don't drop history on every restart.

---

## 11. Running and testing the API

### Prerequisites
- JDK 17+
- A MySQL instance reachable at `localhost:3308` with a database named `dev`
  (or update `application.properties` to point elsewhere) — required only
  because of the chat-memory feature wired into `ChatClientConfig`, not
  because of tool calling itself.
- A Gemini API key.

### Run

```powershell
$env:GEMINI_API_KEY = "your-key-here"
.\mvnw.cmd spring-boot:run
```

### Call it

```powershell
curl -X GET http://localhost:8080/chat `
  -H "username: sovon9" `
  -H "Content-Type: text/plain" `
  -d "What's a good strategy for trend investing in stocks?"
```

What happens behind the scenes for this request:

1. `ChatController` builds a prompt from the body text and attaches
   `username=sovon9` via `toolContext`.
2. Spring AI sends the prompt plus the registered tool's schema
   (`trend-investment`, no parameters) to Gemini.
3. Gemini's reply says, roughly, "the user is asking about trend investing
   strategy — call `trend-investment`."
4. Spring AI invokes `InvestmentTool.trentInvestment(toolContext)`. The log
   line `accessing toolcontext data ========> sovon9` is printed.
5. The tool's returned advice string is sent back to Gemini as the tool
   result.
6. Gemini composes a final natural-language answer, incorporating that
   advice, which is what you see in the HTTP response body.

Try a message that has nothing to do with investing (e.g., `"What's the
capital of France?"`) and you'll see the model answers directly, *without*
ever calling the tool and *without* the log line appearing — proof that
tool invocation is genuinely conditional on the model's judgment, driven by
the `description` text.

---

## 12. Common pitfalls

- **Tool method is not `public`.** Reflection-based invocation fails
  silently/with an exception. Always `public`.
- **Vague `description`.** The single biggest cause of "the model never
  calls my tool" or "the model calls the wrong tool." Be concrete about
  *when* to use it, not just *what* it does.
- **Returning a non-serializable or huge object.** Keep return types to
  simple DTOs/records/Strings; avoid returning entity objects with lazy
  JPA associations (Jackson will choke on proxies) or huge payloads (every
  byte counts against your token budget).
- **Using a tool parameter for secrets/identity.** Don't make the model
  "pass" a user ID, password, or token as a `@ToolParam` — the model could
  hallucinate or be prompt-injected into supplying the wrong one. Use
  `ToolContext` for anything the server already knows and must guarantee.
- **Duplicate tool names** across `defaultTools` and per-call `.tools(...)`
  — causes ambiguous registration errors.
- **Forgetting the tool's owning class needs to be a reachable bean
  instance.** `.defaultTools(tool)` needs an actual object; if you're not
  using DI, you must construct the instance yourself and pass it in.
- **Expecting infinite tool-call loops to be impossible.** In pathological
  cases, a model can call tools repeatedly across several hops before
  answering. This is normal (Spring AI bounds it with an internal max-hop
  safeguard), but very chatty or redundant tool descriptions can make this
  worse — keep tools focused and distinct.
- **GET with a body**, as used in `ChatController`, is non-standard HTTP.
  Works with `curl`/most clients, but some HTTP infrastructure (certain
  proxies, browsers' `fetch`) strips bodies on GET. Prefer `@PostMapping`
  for anything beyond local testing.

---

## 13. Swapping to a different LLM provider

Because the tool-calling API (`@Tool`, `@ToolParam`, `ToolContext`,
`ChatClient.defaultTools(...)`) lives in Spring AI's provider-agnostic core,
switching providers is a **dependency + properties** change only — your
`InvestmentTool` class does not need to change at all:

| Provider      | Starter artifact                              | Key property                        |
|----------------|-----------------------------------------------|--------------------------------------|
| OpenAI         | `spring-ai-starter-model-openai`              | `spring.ai.openai.api-key`           |
| Azure OpenAI   | `spring-ai-starter-model-azure-openai`        | `spring.ai.azure.openai.api-key`     |
| Anthropic      | `spring-ai-starter-model-anthropic`           | `spring.ai.anthropic.api-key`        |
| Google GenAI   | `spring-ai-starter-model-google-genai` (this repo) | `spring.ai.google.genai.api-key`|
| Ollama (local) | `spring-ai-starter-model-ollama`              | `spring.ai.ollama.base-url`          |

Swap the dependency in `pom.xml`, swap the properties block, and everything
in `config/`, `controlller/`, and `tools/` keeps working unchanged. (Actual
tool-calling *reliability* still varies by model/provider — larger,
more capable models follow tool-use instructions more faithfully — but the
Spring AI code you write is identical.)

---

## 14. Further reading

- Spring AI reference docs — Tool Calling:
  https://docs.spring.io/spring-ai/reference/api/tools.html
- Spring AI reference docs — Chat Memory:
  https://docs.spring.io/spring-ai/reference/api/chat-memory.html
- Spring AI reference docs — ChatClient:
  https://docs.spring.io/spring-ai/reference/api/chatclient.html
