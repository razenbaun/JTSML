package io.github.razenbaun.jtsml.export;

import io.github.razenbaun.jtsml.core.TimeSeriesModel;
import io.github.razenbaun.jtsml.models.NaiveModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ModelExporterTest {

    @TempDir
    Path tempDir;

    @Test
    void testSaveAndLoadModel() throws IOException, ClassNotFoundException {
        NaiveModel original = new NaiveModel();
        original.fit(List.of(1.0, 2.0, 3.0));
        Path file = tempDir.resolve("model.ser");
        ModelExporter.saveModel(original, file.toString());

        TimeSeriesModel loaded = ModelExporter.loadModel(file.toString());
        assertNotNull(loaded);
        assertEquals(original.getName(), loaded.getName());

        List<Double> predOrig = original.predict(2);
        List<Double> predLoaded = loaded.predict(2);
        assertEquals(predOrig, predLoaded);
    }

    @Test
    void testExportToJson() {
        NaiveModel model = new NaiveModel();
        model.fit(List.of(1.0));
        String json = ModelExporter.exportToJson(model);
        assertTrue(json.contains("\"name\":\"Naïve\""));
    }
}
