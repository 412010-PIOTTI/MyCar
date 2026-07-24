package ar.edu.utn.frc.mycar.infrastructure.ai;

/** Maps a vehicle brand+model to the manual resource used as RAG context for that vehicle. */
public record ManualDefinition(String brand, String model, String resourcePath) {
}
