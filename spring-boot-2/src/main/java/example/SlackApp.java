package example;

import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.response.users.UsersInfoResponse;
import com.slack.api.methods.response.views.ViewsOpenResponse;
import com.slack.api.model.view.ViewState;
import example.blockview.*;
import example.database.*;
import example.sheet.SheetService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static example.Application.sendMessageToSlack;

@Configuration
public class SlackApp {

    // If you would like to run this app for a single workspace,
    // enabling this Bean factory should work for you.
    @Bean
    public AppConfig loadSingleWorkspaceAppConfig() {
        return AppConfig.builder()
                .singleTeamBotToken(System.getenv("SLACK_BOT_TOKEN"))
                .signingSecret(System.getenv("SLACK_SIGNING_SECRET"))
                .build();
    }

    // If you would like to run this app for multiple workspaces,
    // enabling this Bean factory should work for you.
    //@Bean
    public AppConfig loadOAuthConfig() {
        return AppConfig.builder()
                .singleTeamBotToken(null)
                .clientId(System.getenv("SLACK_CLIENT_ID"))
                .clientSecret(System.getenv("SLACK_CLIENT_SECRET"))
                .signingSecret(System.getenv("SLACK_SIGNING_SECRET"))
                .scope("app_mentions:read,channels:history,channels:read,chat:write")
                .oauthInstallPath("/slack/install")
                .oauthRedirectUriPath("/slack/oauth_redirect")
                .build();
    }
    @Bean
    public App initSlackApp(AppConfig config) {
        App app = new App(config);
        if (config.getClientId() != null) {
            app.asOAuthApp(true);
        }
        app.command("/hello", (req, ctx) -> {
            return ctx.ack(r -> r.text("Hi!"));
        });

        //Store channelId for future use in app.viewSubmission()
        List<String> channelIdList = new ArrayList<>();

        //Clock in !!!
        app.command("/clockin", (req, ctx) -> {
            var channelId = req.getPayload().getChannelId();
            if(channelIdList.size()>0){
                channelIdList.clear();
            }
            channelIdList.add(channelId);

            ViewsOpenResponse viewsOpenRes = ctx.client().viewsOpen(r -> r
                    .triggerId(ctx.getTriggerId())
                    //Build view according to ClockInBlockView.java
                    .view(ClockInBlockView.buildClockInView()));
            if (viewsOpenRes.isOk()) return ctx.ack();
            else return Response.builder().statusCode(500).body(viewsOpenRes.getError()).build();
        });
        app.blockAction("clock-in-timepicker-actionid", (req, ctx) -> {
            // Do something where
            return ctx.ack();
        });
        app.blockAction("company-actionid", (req, ctx) -> {
            // Do something where
            return ctx.ack();
        });
        app.blockAction("what-for-actionid", (req, ctx) -> {
            // Do something where
            return ctx.ack();
        });
        app.blockAction("select-where-actionid", (req, ctx) -> {
            // Do something where
            return ctx.ack();
        });

        app.viewSubmission("clock-in-callbackid", (req, ctx) -> {
            var userId = ctx.getRequestUserId();
            //This is how to get UserFullName
            UsersInfoResponse result = ctx.client().usersInfo(r -> r
                    .token(ctx.getBotToken())
                    .user(userId)
            );
            String UserFullName = result.getUser().getRealName();

            Map<String, Map<String, ViewState.Value>> ResponseMap= req.getPayload().getView().getState().getValues();
            String where = ResponseMap.get("select-where-blockid").get("select-where-actionid").getSelectedOption().getValue();
            String time = ResponseMap.get("clock-in-timepicker-blockid").get("clock-in-timepicker-actionid").getSelectedTime();
            String whatFor = ResponseMap.get("what-for-blockid").get("what-for-actionid").getSelectedOption().getValue();
            String company = ResponseMap.get("company-blockid").get("company-actionid").getSelectedOption().getValue();
            String summary = ResponseMap.get("working-on-input-blockid").get("working-on-input-actionid").getValue();
            LocalDate date = LocalDate.now();

            InsertData insertData = new InsertData();
            String message = insertData.InsertClockIn(String.valueOf(date), time, userId, UserFullName, whatFor, company, summary);

            if(message.equals("Good")) {
                sendMessageToSlack("<@" + userId + ">" + " just clocked in " + where + "!");
                RetrieveData retrieveData = new RetrieveData();
                retrieveData.RetrieveTodayAndUpdate();
            }else{
                //send user private message because they entered invalid data
                var logger = ctx.logger;
                var errorMessage = ctx.client().chatPostEphemeral(r -> r
                        .token(ctx.getBotToken())
                        .channel(channelIdList.get(0))
                        .user(userId)
                        .text(message)
                );
                logger.info("result: {}", errorMessage);
            }
            return ctx.ack();
        });
        app.viewClosed("clock-in-callbackid", (req, ctx) -> {
            // Do something where
            return ctx.ack();
        });

        //Clock out !!!
        app.command("/clockout", (req, ctx) -> {
            var channelId = req.getPayload().getChannelId();
            if(channelIdList.size()>0){
                channelIdList.clear();
            }
            channelIdList.add(channelId);

            ViewsOpenResponse viewsOpenRes = ctx.client().viewsOpen(r -> r
                    .triggerId(ctx.getTriggerId())
                    //Build view according to ClockOutBlockView.java
                    .view(ClockOutBlockView.buildClockOutView()));
            if (viewsOpenRes.isOk()) return ctx.ack();
            else return Response.builder().statusCode(500).body(viewsOpenRes.getError()).build();
        });
        app.blockAction("clock-out-timepicker-actionid", (req, ctx) -> {
            // Do something where
            return ctx.ack();
        });
        app.viewSubmission("clock-out-callbackid", (req, ctx) -> {
            var userId = ctx.getRequestUserId();
            //This is how to get UserFullName
            UsersInfoResponse result = ctx.client().usersInfo(r -> r
                    .token(ctx.getBotToken())
                    .user(userId)
            );
            String UserFullName = result.getUser().getRealName();
            Map<String, Map<String, ViewState.Value>> ResponseMap= req.getPayload().getView().getState().getValues();
            String time = ResponseMap.get("clock-out-timepicker-blockid").get("clock-out-timepicker-actionid").getSelectedTime();
            String summary = ResponseMap.get("what-you-did-input-blockid").get("what-you-did-input-actionid").getValue();
            LocalDate date = LocalDate.now();

            InsertData insertData = new InsertData();
            String message = insertData.InsertClockOut(String.valueOf(date), time,userId, UserFullName, summary);
            // If we have successfully inset data
            if(message.equals("Good")) {
                sendMessageToSlack("<@" + userId + ">" + " just clocked out!");
                RetrieveData retrieveData = new RetrieveData();
                retrieveData.RetrieveTodayAndUpdate();
            }else{
                //send user private message
                var logger = ctx.logger;
                var errorMessage = ctx.client().chatPostEphemeral(r -> r
                        .token(ctx.getBotToken())
                        .channel(channelIdList.get(0))
                        .user(userId)
                        .text(message)
                );
                logger.info("result: {}", errorMessage);
            }
            return ctx.ack();
        });

        app.viewClosed("clock-out-callbackid", (req, ctx) -> {
            // Do something where
            return ctx.ack();
        });

        //Hours Service
        app.command("/hours", (req, ctx) -> {
            var userId = ctx.getRequestUserId();
            var channelId = req.getPayload().getChannelId();
            if(channelIdList.size()>0){
                channelIdList.clear();
            }
            channelIdList.add(channelId);

            Admin admin = new Admin();
            if(!admin.checkAdmin(userId)){
                String message = "Failed. Please contact admin if you need the access";
                var logger = ctx.logger;
                var errorMessage = ctx.client().chatPostEphemeral(r -> r
                        .token(ctx.getBotToken())
                        .channel(channelIdList.get(0))
                        .user(userId)
                        .text(message)
                );
                logger.info("result: {}", errorMessage);
                return ctx.ack();
            }
            ViewsOpenResponse viewsOpenRes = ctx.client().viewsOpen(r -> r
                    .triggerId(ctx.getTriggerId())
                    .view(HoursServiceBlockView.buildHoursView()));
            if (viewsOpenRes.isOk()) return ctx.ack();
            else return Response.builder().statusCode(500).body(viewsOpenRes.getError()).build();
        });

        app.blockAction("hours-start-date-actionid", (req, ctx) -> {
            // Do something where
            return ctx.ack();
        });

        app.blockAction("hours-end-date-actionid", (req, ctx) -> {
            // Do something where
            return ctx.ack();
        });

        app.viewSubmission("hours-callbackid", (req, ctx) -> {
            var userId = ctx.getRequestUserId();
            Map<String, Map<String, ViewState.Value>> ResponseMap= req.getPayload().getView().getState().getValues();
            String startDate = ResponseMap.get("hours-start-date-blockid").get("hours-start-date-actionid").getSelectedDate();
            String endDate = ResponseMap.get("hours-end-date-blockid").get("hours-end-date-actionid").getSelectedDate();

            DateTimeCalculation dtc = new DateTimeCalculation();
            String result = dtc.differenceInDate(startDate,endDate);

            if(Double.valueOf(result)<0){
                String message = "Failed. Ending date prior to starting date is not allowed";
                var logger = ctx.logger;
                var errorMessage = ctx.client().chatPostEphemeral(r -> r
                        .token(ctx.getBotToken())
                        .channel(channelIdList.get(0))
                        .user(userId)
                        .text(message)
                );
                logger.info("result: {}", errorMessage);
            }else{
                RetrieveData retrieveData = new RetrieveData();
                retrieveData.RetrieveHoursAndUpdate(startDate,endDate);
                String message = "Result is shown in the HOURS sheet: \n" +
                        "https://docs.google.com/spreadsheets/d/1mOJ5zz11XJzPCm-pvwQtSKTZv0R14HObwAM7kSg85AE";
                var logger = ctx.logger;
                var Message = ctx.client().chatPostEphemeral(r -> r
                        .token(ctx.getBotToken())
                        .channel(channelIdList.get(0))
                        .user(userId)
                        .text(message)
                );
                logger.info("result: {}", Message);
            }

            return ctx.ack();
        });
        app.viewClosed("hours-callbackid", (req, ctx) -> {
            // Do something where
            return ctx.ack();
        });

        //My previous record
        app.command("/myrecord", (req, ctx) -> {
            var userId = ctx.getRequestUserId();
            var channelId = req.getPayload().getChannelId();
            if(channelIdList.size()>0){
                channelIdList.clear();
            }
            channelIdList.add(channelId);

            RetrieveData rd = new RetrieveData();
            Map<String,String> map = rd.RetrieveRecordById(userId);
            //Handle no date found in database
            ViewsOpenResponse viewsOpenRes = null;
            if(map.containsKey("No")){
                viewsOpenRes = ctx.client().viewsOpen(r -> r
                        .triggerId(ctx.getTriggerId())
                        // return a NoRecordView with no option user can select
                        .view(MyRecordBlockView.buildNoMyRecordView()));
            }else{
                for (String value : map.values()) {
                    viewsOpenRes = ctx.client().viewsOpen(r -> r
                            .triggerId(ctx.getTriggerId())
                            // return a RecordView with options user can select
                            .view(MyRecordBlockView.buildMyRecordView(value)));
                }
            }

            if (viewsOpenRes.isOk()) return ctx.ack();
            else return Response.builder().statusCode(500).body(viewsOpenRes.getError()).build();
        });

        app.blockAction("myrecord-select-action-actionid", (req, ctx) -> {
            // Do something where
            return ctx.ack();
        });

        app.viewSubmission("myrecord-callbackid", (req, ctx) -> {
            var userId = ctx.getRequestUserId();
            Map<String, Map<String, ViewState.Value>> ResponseMap= req.getPayload().getView().getState().getValues();
            String keepOrDelete = ResponseMap.get("myrecord-select-action-blockid").get("myrecord-select-action-actionid").getSelectedOption().getValue();
            if(keepOrDelete.equals("DELETE")){

                DeleteData dd = new DeleteData();
                String message = dd.deleteLastRecord(userId);

                var logger = ctx.logger;
                var Message = ctx.client().chatPostEphemeral(r -> r
                        .token(ctx.getBotToken())
                        .channel(channelIdList.get(0))
                        .user(userId)
                        .text(message)
                );
                logger.info("result: {}", Message);
            }

            return ctx.ack();
        });
        app.viewClosed("myrecord-callbackid", (req, ctx) -> {
            // Do something where
            return ctx.ack();
        });
        // All records
        app.command("/records", (req, ctx) -> {
            var userId = ctx.getRequestUserId();
            var channelId = req.getPayload().getChannelId();
            if(channelIdList.size()>0){
                channelIdList.clear();
            }
            channelIdList.add(channelId);

            Admin admin = new Admin();
            if(!admin.checkAdmin(userId)){
                String message = "Failed. Please contact admin if you need the access";
                var logger = ctx.logger;
                var errorMessage = ctx.client().chatPostEphemeral(r -> r
                        .token(ctx.getBotToken())
                        .channel(channelIdList.get(0))
                        .user(userId)
                        .text(message)
                );
                logger.info("result: {}", errorMessage);
                return ctx.ack();
            }
            ViewsOpenResponse viewsOpenRes = ctx.client().viewsOpen(r -> r
                    .triggerId(ctx.getTriggerId())
                    .view(RecordsBlockView.buildAllRecordsView()));
            if (viewsOpenRes.isOk()) return ctx.ack();
            else return Response.builder().statusCode(500).body(viewsOpenRes.getError()).build();
        });
        app.blockAction("records-start-date-actionid", (req, ctx) -> {
            // Do something where
            return ctx.ack();
        });

        app.blockAction("records-end-date-actionid", (req, ctx) -> {
            // Do something where
            return ctx.ack();
        });

        app.viewSubmission("records-callbackid", (req, ctx) -> {
            var userId = ctx.getRequestUserId();
            Map<String, Map<String, ViewState.Value>> ResponseMap= req.getPayload().getView().getState().getValues();
            String startDate = ResponseMap.get("records-start-date-blockid").get("records-start-date-actionid").getSelectedDate();
            String endDate = ResponseMap.get("records-end-date-blockid").get("records-end-date-actionid").getSelectedDate();

            DateTimeCalculation dtc = new DateTimeCalculation();
            String result = dtc.differenceInDate(startDate,endDate);

            if(Double.valueOf(result)<0){
                String message = "Failed. Ending date prior to starting date is not allowed";
                var logger = ctx.logger;
                var errorMessage = ctx.client().chatPostEphemeral(r -> r
                        .token(ctx.getBotToken())
                        .channel(channelIdList.get(0))
                        .user(userId)
                        .text(message)
                );
                logger.info("result: {}", errorMessage);
            }else{
                RetrieveData retrieveData = new RetrieveData();
                retrieveData.RetrieveRecordsAndUpdate(startDate,endDate);
                String message = "Result is shown in the RECORDS sheet: \n" +
                        "https://docs.google.com/spreadsheets/d/1mOJ5zz11XJzPCm-pvwQtSKTZv0R14HObwAM7kSg85AE";
                var logger = ctx.logger;
                var Message = ctx.client().chatPostEphemeral(r -> r
                        .token(ctx.getBotToken())
                        .channel(channelIdList.get(0))
                        .user(userId)
                        .text(message)
                );
                logger.info("result: {}", Message);
            }

            return ctx.ack();
        });
        app.viewClosed("records-callbackid", (req, ctx) -> {
            // Do something where
            return ctx.ack();
        });

        //Update database from the changes made in sheet
        app.command("/update", (req, ctx) -> {
            var userId = ctx.getRequestUserId();
            var channelId = req.getPayload().getChannelId();
            if(channelIdList.size()>0){
                channelIdList.clear();
            }
            channelIdList.add(channelId);

            Admin admin = new Admin();
            if(!admin.checkAdmin(userId)){
                String message = "Failed. Please contact admin if you need the access";
                var logger = ctx.logger;
                var errorMessage = ctx.client().chatPostEphemeral(r -> r
                        .token(ctx.getBotToken())
                        .channel(channelIdList.get(0))
                        .user(userId)
                        .text(message)
                );
                logger.info("result: {}", errorMessage);
                return ctx.ack();
            }
            ViewsOpenResponse viewsOpenRes = ctx.client().viewsOpen(r -> r
                    .triggerId(ctx.getTriggerId())
                    .view(UpdateBlockView.buildUpdateView()));
            if (viewsOpenRes.isOk()) return ctx.ack();
            else return Response.builder().statusCode(500).body(viewsOpenRes.getError()).build();
        });

        app.blockAction("update-actionid", (req, ctx) -> {
            // Do something where
            return ctx.ack();
        });

        app.viewSubmission("update-callbackid", (req, ctx) -> {
            var userId = ctx.getRequestUserId();
            String sheetId ="1mOJ5zz11XJzPCm-pvwQtSKTZv0R14HObwAM7kSg85AE";
            String range = "RECORDS";
            UpdateData ud = new UpdateData();
            SheetService ss = new SheetService();
            try {
                String message = ud.updateDataFromSheet(ss.ReadAndRetrieveValues(sheetId,range));
                var logger = ctx.logger;
                var Message = ctx.client().chatPostEphemeral(r -> r
                        .token(ctx.getBotToken())
                        .channel(channelIdList.get(0))
                        .user(userId)
                        .text(message)
                );
                logger.info("result: {}", Message);
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
            return ctx.ack();
        });
        app.viewClosed("update-callbackid", (req, ctx) -> {
            // Do something where
            return ctx.ack();
        });

        return app;
    }

}
