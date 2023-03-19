package example.blockview;

import com.slack.api.model.view.View;

import static com.slack.api.model.block.Blocks.asBlocks;
import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.*;
import static com.slack.api.model.block.composition.BlockCompositions.option;
import static com.slack.api.model.block.element.BlockElements.staticSelect;
import static com.slack.api.model.view.Views.*;
import static com.slack.api.model.view.Views.viewClose;

public class MyRecordBlockView {
    public static View buildMyRecordView(String message){

        return view(view -> view
                .callbackId("myrecord-callbackid")
                .type("modal")
                .notifyOnClose(true)
                .title(viewTitle(title -> title.type("plain_text").text("Previous Record").emoji(true)))
                .submit(viewSubmit(submit -> submit.type("plain_text").text("Submit").emoji(true)))
                .close(viewClose(close -> close.type("plain_text").text("Cancel").emoji(true)))
                .privateMetadata("sup!")
                .blocks(asBlocks(
                        section(section -> section
                                .text(markdownText(message))
                                .blockId("myrecord-select-action-blockid")
                                .accessory(staticSelect(staticSelect -> staticSelect
                                        .actionId("myrecord-select-action-actionid")
                                        .placeholder(plainText("Select Action"))
                                        .options(asOptions(
                                                option(plainText("KEEP"), "KEEP"),
                                                option(plainText("DELETE"), "DELETE")
                                        ))
                                ))
                        ),
                        section(section -> section
                                .text(markdownText("(Retrieve last clock-in or clock-out record in 24 hours ago)"))
                        )
                ))
        );
    }
    public static View buildNoMyRecordView(){

        return view(view -> view
                .type("modal")
                .notifyOnClose(true)
                .title(viewTitle(title -> title.type("plain_text").text("Previous Record").emoji(true)))
                .close(viewClose(close -> close.type("plain_text").text("Cancel").emoji(true)))
                .privateMetadata("sup!")
                .blocks(asBlocks(
                        section(section -> section
                                .text(markdownText("No record found for past 24 hours"))
                        ),
                        section(section -> section
                                .text(markdownText("(Retrieve last clock-in or clock-out record in 24 hours ago)"))
                        )
                ))
        );
    }
}
