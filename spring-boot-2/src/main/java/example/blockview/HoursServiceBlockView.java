package example.blockview;

import com.slack.api.model.view.View;

import java.time.LocalDate;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.*;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.*;
import static com.slack.api.model.view.Views.*;
import static com.slack.api.model.view.Views.viewClose;

public class HoursServiceBlockView {
    public static View buildHoursView(){
        LocalDate date = LocalDate.now();

        return view(view -> view
                .callbackId("hours-callbackid")
                .type("modal")
                .notifyOnClose(true)
                .title(viewTitle(title -> title.type("plain_text").text("Hours Service").emoji(true)))
                .submit(viewSubmit(submit -> submit.type("plain_text").text("Submit").emoji(true)))
                .close(viewClose(close -> close.type("plain_text").text("Cancel").emoji(true)))
                .privateMetadata("sup!")
                .blocks(asBlocks(
                        section(section -> section
                                .blockId("hours-start-date-blockid")
                                .text(markdownText("Pick the starting Date:"))
                                .accessory(datePicker(tp -> tp.actionId("hours-start-date-actionid")
                                        .initialDate(String.valueOf(date))
                                        .placeholder(plainText("Pick a starting date:"))))
                        ),
                        section(section -> section
                                .blockId("hours-end-date-blockid")
                                .text(markdownText("Pick the Ending Date:"))
                                .accessory(datePicker(tp -> tp.actionId("hours-end-date-actionid")
                                        .initialDate(String.valueOf(date))
                                        .placeholder(plainText("Pick an ending date:"))))
                        ),
                        section(section -> section
                                .text(markdownText("(Calculate hours for every staff who has records in this time period)"))
                        )
                ))
        );
    }
}
