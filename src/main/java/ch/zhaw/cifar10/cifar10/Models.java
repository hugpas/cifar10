package ch.zhaw.cifar10.cifar10;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import ai.djl.Model;
import ai.djl.basicmodelzoo.cv.classification.ResNetV1;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Block;

public final class Models {

    // Anzahl der Klassifikationslabels: 10
    public static final int NUM_OF_OUTPUT = 10;

    // Abmessungen der Cifar-10 Bilder
    public static final int IMAGE_HEIGHT = 32;
    public static final int IMAGE_WIDTH = 32;

    // Modellname
    public static final String MODEL_NAME = "cifar10-classification";

    private Models() {}

    public static Model getModel() {
        // Neue Modellinstanz erstellen
        Model model = Model.newInstance(MODEL_NAME);

        // ResNet50 Block
        Block resNet50 = ResNetV1.builder()
                .setImageShape(new Shape(3, IMAGE_HEIGHT, IMAGE_WIDTH))
                .setNumLayers(50)
                .setOutSize(NUM_OF_OUTPUT)
                .build();

        // Netzwerk Block im Modell setzen
        model.setBlock(resNet50);
        return model;
    }
    
    public static void saveSynset(Path modelDir, List<String> synset) throws IOException {
        Path synsetFile = modelDir.resolve("synset.txt");
        try (Writer writer = Files.newBufferedWriter(synsetFile)) {
            writer.write(String.join("\n", synset));
        }
    }

}
