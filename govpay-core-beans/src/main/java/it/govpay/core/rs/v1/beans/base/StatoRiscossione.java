package it.govpay.core.rs.v1.beans.base;

import java.util.Objects;
import org.codehaus.jackson.annotate.JsonValue;

/**
 * Stato della riscossione
 **/


/**
 * Stato della riscossione
 */
public enum StatoRiscossione {
  
  
  
  
  RISCOSSA("RISCOSSA"),
  
  
  INCASSATA("INCASSATA");
  
  
  

  private String value;

  StatoRiscossione(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  public static StatoRiscossione fromValue(String text) {
    for (StatoRiscossione b : StatoRiscossione.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}


