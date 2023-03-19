package example.blockview;

import com.slack.api.model.view.View;

import java.time.LocalDate;

import static com.slack.api.model.block.Blocks.asBlocks;
import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.datePicker;
import static com.slack.api.model.view.Views.*;
import static com.slack.api.model.view.Views.viewClose;

public class RecordsBlockView {
    public static View buildAllRecordsView(){
        LocalDate date = LocalDate.now();

        return view(view -> view
                .callbackId("records-callbackid")
                .type("modal")
                .notifyOnClose(true)
                .title(viewTitle(title -> title.type("plain_text").text("Records Service").emoji(true)))
                .submit(viewSubmit(submit -> submit.type("plain_text").text("Submit").emoji(true)))
                .close(viewClose(close -> close.type("plain_text").text("Cancel").emoji(true)))
                .privateMetadata("sup!")
                .blocks(asBlocks(
                        section(section -> section
                                .blockId("records-start-date-blockid")
                                .text(markdownText("Pick the starting Date:"))
                                .accessory(datePicker(tp -> tp.actionId("records-start-date-actionid")
                                        .initialDate(String.valueOf(date))
                                        .placeholder(plainText("Pick a starting date:"))))
                        ),
                        section(section -> section
                                .blockId("records-end-date-blockid")
                                .text(markdownText("Pick the Ending Date:"))
                                .accessory(datePicker(tp -> tp.actionId("records-end-date-actionid")
                                        .initialDate(String.valueOf(date))
                                        .placeholder(plainText("Pick an ending date:"))))
                        ),
                        section(section -> section
                                .text(markdownText("(Retrieve all records in this time period)"))
                        )
                ))
        );
    }
}
