package com.sovon9.Simple_LLM_Api.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class InvestmentTool {

    Logger LOGGER = LoggerFactory.getLogger(InvestmentTool.class);

    /**
     *
     * @param toolContext
     * @return
     */
    @Tool(name = "trend-investment", description = "tells about the best way to earn in a trent stock market")
    public String trentInvestment(ToolContext toolContext)
    {
        // accessing tool-context data
        LOGGER.error("accessing toolcontext data ========> "+toolContext.getContext().get("username"));

        return "always check shares in a multi timeframe and identify the trend using monthly chart and then use that chart to plot" +
                "20MA and 50MA and using that see if the current position of the share is in above those lines or not. If current position is above 20-50MA then" +
                "wait for a pullback and buy it";
    }

}
