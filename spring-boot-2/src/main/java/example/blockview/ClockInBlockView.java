package example.blockview;

import com.slack.api.model.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.slack.api.model.Attachments.action;
import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.*;
import static com.slack.api.model.block.composition.BlockCompositions.option;
import static com.slack.api.model.block.element.BlockElements.*;
import static com.slack.api.model.view.Views.*;
import static com.slack.api.model.view.Views.viewClose;

public class ClockInBlockView {
    public static View buildClockInView(){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        String initialTime = sdf.format(date);

        return view(view -> view
                .callbackId("clock-in-callbackid")
                .type("modal")
                .notifyOnClose(true)
                .title(viewTitle(title -> title.type("plain_text").text("Clocking In").emoji(true)))
                .submit(viewSubmit(submit -> submit.type("plain_text").text("Submit").emoji(true)))
                .close(viewClose(close -> close.type("plain_text").text("Cancel").emoji(true)))
                .privateMetadata("sup!")
                .blocks(asBlocks(
                        section(section -> section
                                .blockId("clock-in-timepicker-blockid")
                                .text(markdownText("Enter the time you clock in (MST):"))
                                .accessory(timePicker(tp -> tp.actionId("clock-in-timepicker-actionid")
                                        .initialTime(initialTime)
                                        .placeholder(plainText("Enter time"))))
                        ),
                        section(section -> section
                                .text(markdownText("What company is this for?"))
                                .blockId("company-blockid")
                                .accessory(staticSelect(staticSelect -> staticSelect
                                        .actionId("company-actionid")
                                        .placeholder(plainText("Select a company"))
                                        .options(asOptions(
                                                option(plainText("EpiBuild"), "EpiBuild"),
                                                option(plainText("EpiFinder"), "EpiFinder")
                                        ))
                                ))
                        ),
                        section(section -> section
                                .text(markdownText("What is this for?"))
                                .blockId("what-for-blockid")
                                .accessory(staticSelect(staticSelect -> staticSelect
                                        .actionId("what-for-actionid")
                                        .placeholder(plainText("Select an option"))
                                        .options(asOptions(
                                                option(plainText("Training - P/E/M Section"), "Training - P/E/M Section"),
                                                option(plainText("Training - Research"), "Training - Research"),
                                                option(plainText("HR Duty"), "HR Duty"),
                                                option(plainText("Marketing Duty"), "Marketing Duty"),
                                                option(plainText("Sales Duty"), "Sales Duty"),
                                                option(plainText("Management"), "Management"),
                                                option(plainText("App Project"), "App Project"),
                                                option(plainText("Website Project"), "Website Project"),
                                                option(plainText("Marketing Project"), "Marketing Project"),
                                                option(plainText("Legal Research Project"), "Legal Research Project")
                                        ))
                                ))
                        ),
                        section(section -> section
                                .text(markdownText("Where will you be working at?"))
                                .blockId("select-where-blockid")
                                .accessory(staticSelect(staticSelect -> staticSelect
                                        .actionId("select-where-actionid")
                                        .placeholder(plainText("Select an option"))
                                                .options(asOptions(
                                                        option(plainText("Remotely"), "remotely"),
                                                        option(plainText("In the office"), "in the office")
                                                ))
                                ))
                        ),
                        input(input -> input
                                .blockId("working-on-input-blockid")
                                .element(plainTextInput(pti -> pti.actionId("working-on-input-actionid").multiline(true)))
                                .label(plainText(pt -> pt.text("What will you be working on?")))
                             )
                ))
        );
    }
}
