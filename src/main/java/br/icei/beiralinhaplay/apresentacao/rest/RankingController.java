package br.icei.beiralinhaplay.apresentacao.rest;

import br.icei.beiralinhaplay.aplicacao.ranking.RankingService;
import br.icei.beiralinhaplay.apresentacao.dto.DtoConverter;
import br.icei.beiralinhaplay.apresentacao.dto.RankingResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rankings")
public class RankingController {

    private final RankingService servicoRanking;

    public RankingController(RankingService servicoRanking) {
        this.servicoRanking = servicoRanking;
    }

    @GetMapping
    public List<RankingResponse> listar(@RequestParam(required = false) String cursoId) {
        UUID curso = cursoId == null || cursoId.isBlank() ? null : DtoConverter.id(cursoId);
        return servicoRanking.listar(curso).stream().map(DtoConverter::ranking).toList();
    }
}
