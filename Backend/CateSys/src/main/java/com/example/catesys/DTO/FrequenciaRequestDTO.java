package com.example.catesys.DTO;

import java.util.Date;
import java.util.List;

public record FrequenciaRequestDTO(Date data, int turma_id, List<Long> alunos) {}
