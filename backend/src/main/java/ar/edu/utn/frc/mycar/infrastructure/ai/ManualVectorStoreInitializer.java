package ar.edu.utn.frc.mycar.infrastructure.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * Embeds the manufacturer manuals into the vector store on startup.
 *
 * <p>If a previously persisted vector store file exists on disk, it is loaded instead of
 * re-embedding — embeddings are billed OpenAI API calls, so this avoids paying for (and waiting
 * on) the same embeddings on every restart.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ManualVectorStoreInitializer implements ApplicationRunner {

    private final SimpleVectorStore vectorStore;
    private final ManualCatalog manualCatalog;

    @Value("${app.ai.vector-store.path}")
    private String vectorStorePath;

    @Value("${app.ai.manuals-ingestion.enabled:true}")
    private boolean ingestionEnabled;

    @Override
    public void run(ApplicationArguments args) {
        if (!ingestionEnabled) {
            log.info("Manual ingestion disabled (app.ai.manuals-ingestion.enabled=false) — skipping.");
            return;
        }

        // Never let a failure here (e.g. an OpenAI billing/quota error) take down the whole
        // backend — the rest of the app must stay usable even if the AI chat can't be grounded.
        try {
            loadOrEmbedManuals();
        } catch (Exception ex) {
            log.error("Failed to initialise the manual vector store — the AI chat will run without "
                    + "manual context until this is resolved and the backend is restarted.", ex);
        }
    }

    private void loadOrEmbedManuals() {
        File file = new File(vectorStorePath);
        if (file.exists()) {
            log.info("Loading existing manual vector store from {}", file.getAbsolutePath());
            vectorStore.load(file);
            return;
        }

        log.info("No persisted vector store found at {} — embedding manuals now.", file.getAbsolutePath());
        TokenTextSplitter splitter = TokenTextSplitter.builder().build();

        for (ManualDefinition manual : manualCatalog.all()) {
            TextReader reader = new TextReader(new ClassPathResource(manual.resourcePath()));
            reader.getCustomMetadata().put("brand", manual.brand());
            reader.getCustomMetadata().put("model", manual.model());

            List<Document> chunks = splitter.apply(reader.get());
            vectorStore.add(chunks);
            log.info("Embedded manual for {} {} ({} chunks)", manual.brand(), manual.model(), chunks.size());
        }

        File parentDir = file.getParentFile();
        if (parentDir != null) {
            parentDir.mkdirs();
        }
        vectorStore.save(file);
        log.info("Saved manual vector store to {}", file.getAbsolutePath());
    }
}
