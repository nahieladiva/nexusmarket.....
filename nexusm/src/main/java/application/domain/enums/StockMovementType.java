package application.domain.enums;

/**
 * Tipos de movimiento de inventario que quedan registrados en el historial.
 */
public enum StockMovementType {
    /** Registro del stock inicial de un producto en una bodega. */
    INITIAL,
    /** Entrada de mercancía (reabastecimiento). */
    RESTOCK,
    /** Ajuste manual (conteo físico, merma, daño). Puede ser positivo o negativo. */
    ADJUSTMENT,
    /** Salida por reserva al confirmar un pedido. */
    RESERVATION,
    /** Devolución de stock al cancelar un pedido confirmado. */
    RELEASE
}
