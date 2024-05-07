package ch.zhaw.cifar10.cifar10;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import ai.djl.Model;
import ai.djl.basicdataset.cv.classification.Cifar10;
import ai.djl.metric.Metrics;
import ai.djl.ndarray.types.Shape;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.TrainingConfig;
import ai.djl.training.TrainingResult;
import ai.djl.training.evaluator.Accuracy;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.translate.TranslateException;

public final class Training {
    
    private static final int BATCH_SIZE = 32;
    private static final int EPOCHS = 10;
    
    public static void main(String[] args) throws IOException, TranslateException {
        Path modelDir = Paths.get("models");

        // Cifar-10 Datensatz initialisieren
        Cifar10 trainDataset = Cifar10.builder()
                .optUsage(Cifar10.Usage.TRAIN)
                .setSampling(BATCH_SIZE, true)
                .build();
        Cifar10 testDataset = Cifar10.builder()
                .optUsage(Cifar10.Usage.TEST)
                .setSampling(BATCH_SIZE, true)
                .build();

        // Datensätze vorbereiten
        trainDataset.prepare();
        testDataset.prepare();

        // Training Konfigurieren
        Loss loss = Loss.softmaxCrossEntropyLoss();
        TrainingConfig config = new DefaultTrainingConfig(loss)
                .addEvaluator(new Accuracy())
                .addTrainingListeners(TrainingListener.Defaults.logging());
        
        // Modell und Trainer erstellen
        Model model = Models.getModel();
        Trainer trainer = model.newTrainer(config);
        trainer.setMetrics(new Metrics());

        // Modell mit Eingabeform initialisieren
        Shape inputShape = new Shape(1, 3, Models.IMAGE_WIDTH, Models.IMAGE_HEIGHT);
        trainer.initialize(inputShape);

        // Modell trainieren
        EasyTrain.fit(trainer, EPOCHS, trainDataset, testDataset);

        // Modell speichern
        TrainingResult result = trainer.getTrainingResult();
        model.setProperty("Epoch", String.valueOf(EPOCHS));
        model.setProperty("Accuracy", String.format("%.5f", result.getValidateEvaluation("Accuracy")));
        model.setProperty("Loss", String.format("%.5f", result.getValidateLoss()));

        model.save(modelDir, Models.MODEL_NAME);
        
    }

}
