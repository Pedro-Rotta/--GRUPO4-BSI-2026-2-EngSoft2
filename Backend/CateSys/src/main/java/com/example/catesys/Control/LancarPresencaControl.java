package com.example.catesys.Control;

import com.example.catesys.DAO.FrequenciaDAO;
import com.example.catesys.DAO.FrequenciaFaltaDAO;
import com.example.catesys.DTO.FrequenciaFaltaResponseDTO;
import com.example.catesys.DTO.FrequenciaRequestDTO;
import com.example.catesys.Model.Frequencia;
import com.example.catesys.Model.FrequenciaFalta;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/presenca")
public class LancarPresencaControl {
    private final FrequenciaDAO frequenciaDAO;
    private final FrequenciaFaltaDAO frequenciaFaltaDAO;

    public LancarPresencaControl(FrequenciaDAO frequenciaDAO, FrequenciaFaltaDAO frequenciaFaltaDAO) {
        this.frequenciaDAO = frequenciaDAO;
        this.frequenciaFaltaDAO = frequenciaFaltaDAO;
    }

    @PostMapping("/lancar-presenca")
    public ResponseEntity lancarPresenca(@RequestBody FrequenciaRequestDTO body) {

        if (!body.data().after(new Date())) {
            Frequencia frequencia = new Frequencia(body.data(), body.turma_id());
            FrequenciaFalta frequenciaFalta = new FrequenciaFalta(body.data(), body.turma_id());
            List<Long> alunos = body.alunos();
            List<FrequenciaFalta> faltas = new ArrayList<>();
            try {
                frequenciaDAO.gravar(frequencia);
                for(Long l : alunos) {
                    frequenciaFalta.setMatriculaId(l.intValue());
                    frequenciaFaltaDAO.gravar(frequenciaFalta);
                    faltas.add(frequenciaFalta);
                }
                return ResponseEntity.ok(new FrequenciaFaltaResponseDTO(frequencia, faltas));
            }
            catch (SQLException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        else {
            return ResponseEntity.unprocessableContent().build();
        }
    }
}
