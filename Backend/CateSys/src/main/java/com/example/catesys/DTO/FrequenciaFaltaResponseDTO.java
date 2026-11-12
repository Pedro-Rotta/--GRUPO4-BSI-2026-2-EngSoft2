package com.example.catesys.DTO;

import com.example.catesys.Model.Frequencia;
import com.example.catesys.Model.FrequenciaFalta;

import java.util.List;

public record FrequenciaFaltaResponseDTO(Frequencia frequencia, List<FrequenciaFalta> faltas) {
}
