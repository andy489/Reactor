package com.relax.reactor.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.relax.reactor.config.BaseDtoDeserializer;
import com.relax.reactor.service.gamelogic.dto.SlotGameDto;
import com.relax.reactor.service.gamelogic.dto.payout.BaseDto;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Getter
public class SpinResultLogger {

    private static final String LAST_SPIN_DIR = "last_spin/";
    private static final String ORIGINAL_SPIN_FILE = "original_spin.json";

    private final Logger LOGGER = LoggerFactory.getLogger(SpinResultLogger.class);
    private final ObjectMapper mapper;

    public SpinResultLogger(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @PostConstruct
    public void init() {
        try {
            Path dirPath = Paths.get(LAST_SPIN_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                LOGGER.info("Created directory for spin results: {}", LAST_SPIN_DIR);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to initialize SpinResultLogger", e);
            throw new RuntimeException("Failed to initialize SpinResultLogger: ", e);
        }
    }

    // Save original non-linearized spin
    public void logOriginalSpin(SlotGameDto originalSpin) {
        try {
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(originalSpin);

            String filePath = LAST_SPIN_DIR + ORIGINAL_SPIN_FILE;
            try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
                writer.print(json);
            }

            LOGGER.info("Logged original non-linearized spin to file: {}", filePath);

        } catch (Exception e) {
            LOGGER.error("Failed to log original spin", e);
            throw new RuntimeException("Failed to log original spin", e);
        }
    }

    // Load original non-linearized spin
    public SlotGameDto loadOriginalSpin() {
        try {
            String filePath = LAST_SPIN_DIR + ORIGINAL_SPIN_FILE;
            File spinFile = new File(filePath);

            if (spinFile.exists() && spinFile.length() > 0) {
                LOGGER.debug("Loading original spin from file: {}", filePath);

                String jsonContent = new String(Files.readAllBytes(Paths.get(filePath)));

                ObjectMapper mapper = new ObjectMapper();

                SimpleModule module = new SimpleModule();
                module.addDeserializer(BaseDto.class, new BaseDtoDeserializer());
                mapper.registerModule(module);

                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                return mapper.readValue(jsonContent, SlotGameDto.class);
            } else {
                LOGGER.debug("No original spin file found or file is empty");
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load original spin from file", e);
            return null;
        }
    }
}