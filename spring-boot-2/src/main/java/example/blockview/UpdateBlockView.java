package example.blockview;

import com.slack.api.model.view.View;

import static com.slack.api.model.block.Blocks.asBlocks;
import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.*;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.element.BlockElements.staticSelect;
import static com.slack.api.model.view.Views.*;
import static com.slack.api.model.view.Views.viewClose;

public class UpdateBlockView {
    public static View buildUpdateView(){

        return view(view -> view
                .callbackId("update-callbackid")
                .type("modal")
                .notifyOnClose(true)
                .title(viewTitle(title -> title.type("plain_text").text("Update Service").emoji(true)))
                .submit(viewSubmit(submit -> submit.type("plain_text").text("Submit").emoji(true)))
                .close(viewClose(close -> close.type("plain_text").text("Cancel").emoji(true)))
                .privateMetadata("sup!")
                .blocks(asBlocks(
                        section(section -> section
                                .text(markdownText("Are you sure you want to update changes made in the RECORDS Google sheet to database?"))
                                .blockId("update-blockid")
                        )
                ))
        );
    }
}
