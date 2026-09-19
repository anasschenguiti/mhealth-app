package com.example.application_final.medcin;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ImageClassifier {
    private static final String TAG = "ImageClassifier";
    private static final int IMAGE_SIZE = 224;
    private static final int NUM_CLASSES = 3; // Pas de maladie, Maladie chronique, Cas d'urgence
    /** Taille exacte pour modèle quantifié UINT8 : 224 × 224 × 3 = 150 528 octets */
    private static final int EXPECTED_INPUT_BYTES = IMAGE_SIZE * IMAGE_SIZE * 3;
    
    private Interpreter tflite;
    private List<String> labels;
    private Context context;
    private boolean isInitialized = false;
    private String initializationError = null;
    
    public ImageClassifier(Context context) {
        this.context = context;
        initialize();
    }
    
    /**
     * Initialise le classificateur avec gestion d'erreurs robuste
     */
    private void initialize() {
        try {
            // Charger le modèle
            ByteBuffer modelBuffer = loadModelFile();
            if (modelBuffer == null) {
                initializationError = "Impossible de charger le fichier modèle";
                Log.e(TAG, initializationError);
                return;
            }
            
            // Initialiser l'interpréteur TensorFlow Lite avec options pour modèle quantifié
            try {
                Interpreter.Options options = new Interpreter.Options();
                options.setNumThreads(4); // Utiliser 4 threads pour améliorer les performances
                tflite = new Interpreter(modelBuffer, options);
                Log.d(TAG, "Modèle TensorFlow Lite quantifié (UINT8) chargé avec succès");
            } catch (Exception e) {
                initializationError = "Erreur lors de l'initialisation de l'interpréteur: " + e.getMessage();
                Log.e(TAG, initializationError, e);
                return;
            }
            
            // Charger les labels
            try {
                labels = loadLabelList();
                if (labels == null || labels.isEmpty()) {
                    initializationError = "Aucun label chargé depuis le fichier labels.txt";
                    Log.e(TAG, initializationError);
                    return;
                }
                Log.d(TAG, "Labels chargés avec succès: " + labels.size() + " classes");
            } catch (Exception e) {
                initializationError = "Erreur lors du chargement des labels: " + e.getMessage();
                Log.e(TAG, initializationError, e);
                return;
            }
            
            isInitialized = true;
            Log.d(TAG, "Classificateur initialisé avec succès");
            
        } catch (Exception e) {
            initializationError = "Erreur générale lors de l'initialisation: " + e.getMessage();
            Log.e(TAG, initializationError, e);
            e.printStackTrace();
        }
    }
    
    /**
     * Vérifie si le classificateur est correctement initialisé
     */
    public boolean isInitialized() {
        return isInitialized && tflite != null && labels != null && !labels.isEmpty();
    }
    
    /**
     * Retourne le message d'erreur d'initialisation si disponible
     */
    public String getInitializationError() {
        return initializationError;
    }
    
    /**
     * Retourne le chemin du modèle TensorFlow Lite
     */
    protected String getModelPath() {
        return "converted_tflite_quantized/model.tflite";
    }
    
    /**
     * Retourne le chemin du fichier de labels
     */
    protected String getLabelPath() {
        return "converted_tflite_quantized/labels.txt";
    }
    
    /**
     * Charge le modèle TensorFlow Lite depuis les assets avec gestion d'erreurs
     */
    private ByteBuffer loadModelFile() {
        String modelPath = getModelPath();
        InputStream inputStream = null;
        
        try {
            inputStream = context.getAssets().open(modelPath);
            int available = inputStream.available();
            
            if (available <= 0) {
                Log.e(TAG, "Le fichier modèle est vide ou n'existe pas: " + modelPath);
                return null;
            }
            
            byte[] buffer = new byte[available];
            int bytesRead = inputStream.read(buffer);
            
            if (bytesRead != available) {
                Log.e(TAG, "Erreur lors de la lecture du fichier modèle. Lu: " + bytesRead + ", Attendu: " + available);
                return null;
            }
            
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(buffer.length);
            byteBuffer.order(ByteOrder.nativeOrder());
            byteBuffer.put(buffer);
            byteBuffer.rewind();
            
            Log.d(TAG, "Modèle chargé: " + buffer.length + " bytes depuis " + modelPath);
            return byteBuffer;
            
        } catch (IOException e) {
            Log.e(TAG, "Erreur IO lors du chargement du modèle depuis: " + modelPath, e);
            Log.e(TAG, "Vérifiez que le dossier 'assets' existe et contient le fichier modèle");
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Erreur inattendue lors du chargement du modèle", e);
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Erreur lors de la fermeture du stream", e);
                }
            }
        }
    }
    
    /**
     * Charge la liste des labels depuis le fichier labels.txt avec gestion d'erreurs
     */
    private List<String> loadLabelList() {
        List<String> labelList = new ArrayList<>();
        String labelPath = getLabelPath();
        InputStream inputStream = null;
        Scanner scanner = null;
        
        try {
            inputStream = context.getAssets().open(labelPath);
            scanner = new Scanner(inputStream);
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    // Format: "0 Pas de maladie" -> extraire "Pas de maladie"
                    String[] parts = line.split("\\s+", 2);
                    if (parts.length >= 2) {
                        labelList.add(parts[1]);
                    } else {
                        labelList.add(line);
                    }
                }
            }
            
            Log.d(TAG, "Labels chargés: " + labelList.size() + " classes");
            return labelList;
            
        } catch (IOException e) {
            Log.e(TAG, "Erreur IO lors du chargement des labels depuis: " + labelPath, e);
            Log.e(TAG, "Vérifiez que le dossier 'assets' existe et contient le fichier labels.txt");
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Erreur inattendue lors du chargement des labels", e);
            return null;
        } finally {
            if (scanner != null) {
                scanner.close();
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Erreur lors de la fermeture du stream", e);
                }
            }
        }
    }
    
    /**
     * Classifie une image Bitmap et retourne le résultat
     */
    public ClassificationResult classifyImage(Bitmap bitmap) {
        if (!isInitialized()) {
            String errorMsg = initializationError != null ? initializationError : "Le modèle n'est pas initialisé";
            Log.e(TAG, errorMsg);
            return new ClassificationResult("Erreur", errorMsg, 0.0f);
        }
        
        if (bitmap == null) {
            return new ClassificationResult("Erreur", "L'image fournie est nulle", 0.0f);
        }
        
        try {
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true);
            ByteBuffer byteBuffer = convertBitmapToByteBuffer(resizedBitmap);
            if (byteBuffer.capacity() != EXPECTED_INPUT_BYTES) {
                return new ClassificationResult("Erreur", "Buffer entrée invalide: " + byteBuffer.capacity() + " != " + EXPECTED_INPUT_BYTES, 0.0f);
            }

            tflite.run(byteBuffer, null);

            Tensor outputTensor = tflite.getOutputTensor(0);
            int[] shape = outputTensor.shape();
            int numClasses = shape.length > 1 ? shape[1] : shape[0];
            float[] probabilities = readOutputAndDequantize(outputTensor, numClasses);

            float maxProb = 0.0f;
            int maxIndex = 0;
            for (int i = 0; i < numClasses && i < probabilities.length; i++) {
                if (probabilities[i] > maxProb) {
                    maxProb = probabilities[i];
                    maxIndex = i;
                }
            }

            String label = maxIndex < labels.size() ? labels.get(maxIndex) : "Inconnu";
            return new ClassificationResult(label, getUrgencyLevel(label), maxProb);

        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la classification", e);
            return new ClassificationResult("Erreur", "Erreur lors de la classification: " + e.getMessage(), 0.0f);
        }
    }
    
    /**
     * Convertit un Bitmap en ByteBuffer pour l'inférence (modèle quantifié UINT8).
     * Pixels stockés en bytes 0–255, sans normalisation (/255).
     * Taille exacte : 224 × 224 × 3 = 150 528 octets.
     */
    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(EXPECTED_INPUT_BYTES);
        byteBuffer.order(ByteOrder.nativeOrder());
        
        int[] intValues = new int[IMAGE_SIZE * IMAGE_SIZE];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        
        int pixel = 0;
        for (int i = 0; i < IMAGE_SIZE; ++i) {
            for (int j = 0; j < IMAGE_SIZE; ++j) {
                final int val = intValues[pixel++];
                byteBuffer.put((byte) ((val >> 16) & 0xFF)); // R, 0–255
                byteBuffer.put((byte) ((val >> 8) & 0xFF));  // G, 0–255
                byteBuffer.put((byte) (val & 0xFF));         // B, 0–255
            }
        }
        
        byteBuffer.rewind();
        Log.d(TAG, "Image convertie en ByteBuffer UINT8: " + byteBuffer.capacity() + " bytes");
        return byteBuffer;
    }
    
    /**
     * Lit la sortie du modèle et la convertit en float[].
     * Si sortie UINT8 quantifiée : déquantification avec scale/zero_point.
     * Si sortie FLOAT32 : lecture directe.
     */
    private float[] readOutputAndDequantize(Tensor outputTensor, int numClasses) {
        ByteBuffer buffer = outputTensor.asReadOnlyBuffer();
        buffer.rewind();
        float[] out = new float[numClasses];
        DataType dtype = outputTensor.dataType();

        if (dtype == DataType.UINT8) {
            Tensor.QuantizationParams q = outputTensor.quantizationParams();
            float scale = q.getScale();
            int zeroPoint = q.getZeroPoint();
            for (int i = 0; i < numClasses && buffer.hasRemaining(); i++) {
                int u8 = buffer.get() & 0xFF;
                out[i] = (u8 - zeroPoint) * scale;
            }
            Log.d(TAG, "Sortie UINT8 déquantifiée (scale=" + scale + ", zeroPoint=" + zeroPoint + ")");
        } else if (dtype == DataType.FLOAT32) {
            buffer.order(ByteOrder.nativeOrder());
            FloatBuffer fb = buffer.asFloatBuffer();
            for (int i = 0; i < numClasses && i < fb.remaining(); i++) {
                out[i] = fb.get(i);
            }
            Log.d(TAG, "Sortie FLOAT32 lue directement");
        } else {
            Log.w(TAG, "Type de sortie non géré: " + dtype + ", fallback (raw bytes / 255)");
            for (int i = 0; i < numClasses && buffer.hasRemaining(); i++) {
                out[i] = (buffer.get() & 0xFF) / 255.0f;
            }
        }
        return out;
    }

    /**
     * Détermine le niveau d'urgence basé sur la classification
     */
    private String getUrgencyLevel(String label) {
        if (label.contains("urgence") || label.contains("Cas d'urgence")) {
            return "URGENT - Intervention immédiate requise";
        } else if (label.contains("chronique") || label.contains("Maladie chronique")) {
            return "MOYEN - Suivi médical nécessaire";
        } else if (label.contains("Pas de maladie")) {
            return "FAIBLE - Aucune pathologie détectée";
        }
        return "Niveau d'urgence non déterminé";
    }
    
    /**
     * Libère les ressources
     */
    public void close() {
        if (tflite != null) {
            tflite.close();
            tflite = null;
        }
    }
    
    /**
     * Classe pour stocker le résultat de la classification
     */
    public static class ClassificationResult {
        private String label;
        private String urgencyLevel;
        private float confidence;
        
        public ClassificationResult(String label, String urgencyLevel, float confidence) {
            this.label = label;
            this.urgencyLevel = urgencyLevel;
            this.confidence = confidence;
        }
        
        public String getLabel() {
            return label;
        }
        
        public String getUrgencyLevel() {
            return urgencyLevel;
        }
        
        public float getConfidence() {
            return confidence;
        }
        
        public String getConfidencePercentage() {
            float p = Math.max(0f, Math.min(1f, confidence));
            return String.format("%.1f%%", p * 100);
        }
    }
}

