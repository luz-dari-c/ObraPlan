package co.obraplan;

import java.util.Map;
import java.util.function.Function;

/** Contrato transaccional. El servicio no necesita conocer archivos ni SQL. */
interface AlmacenEstado {
    Map<String, Object> leer();

    <T> T modificar(Function<Map<String, Object>, T> operacion);
}
