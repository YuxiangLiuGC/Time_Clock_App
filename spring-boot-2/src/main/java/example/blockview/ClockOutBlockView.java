package example.blockview;

import com.slack.api.model.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.*;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.*;
import static com.slack.api.model.view.Views.*;
import static com.slack.api.model.view.Views.viewClose;

public class ClockOutBlockView {
    public static View buildClockOutView(){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        String initialTime = sdf.format(date);

        return view(view -> view
                .callbackId("clock-out-callbackid")
                .type("modal")
                .notifyOnClose(true)
                .title(viewTitle(title -> title.type("plain_text").text("Clocking Out").emoji(true)))
                .submit(viewSubmit(submit -> submit.type("plain_text").text("Submit").emoji(true)))
                .close(viewClose(close -> close.type("plain_text").text("Cancel").emoji(true)))
                .privateMetadata("sup!")
                .blocks(asBlocks(
                        section(section -> section
                                .blockId("clock-out-timepicker-blockid")
                                .text(markdownText("Enter the time you clock out (MST):"))
                                .accessory(timePicker(tp -> tp.actionId("clock-out-timepicker-actionid")
                                        .initialTime(initialTime)
                                        .placeholder(plainText("Enter time"))))
                        ),
                        section(section -> section
                                        .text(markdownText("(For each project type, you must clock in and out separately)"))
                        ),
                        input(input -> input
                                .blockId("what-you-did-input-blockid")
                                .element(plainTextInput(pti -> pti.actionId("what-you-did-input-actionid").multiline(true)))
                                .label(plainText(pt -> pt.text("What did you work on today?")))
                        )
                ))
        );
    }
}
