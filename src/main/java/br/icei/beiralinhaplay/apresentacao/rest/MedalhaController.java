package br.icei.beiralinhaplay.apresentacao.rest;

import br.icei.beiralinhaplay.aplicacao.medalha.MedalhaService;
import br.icei.beiralinhaplay.aplicacao.medalha.SalvarMedalhaCommand;
import br.icei.beiralinhaplay.apresentacao.dto.DtoConverter;
import br.icei.beiralinhaplay.apresentacao.dto.MedalhaResponse;
import br.icei.beiralinhaplay.apresentacao.dto.UsuarioResponse;
import br.icei.beiralinhaplay.dominio.compartilhado.BusinessRuleException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/medalhas")
public class MedalhaController {

    private final MedalhaService servicoMedalha;

    public MedalhaController(MedalhaService servicoMedalha) {
        this.servicoMedalha = servicoMedalha;
    }

    @GetMapping
    public List<MedalhaResponse> listar(Authentication authentication) {
        return servicoMedalha.listar(AuthHttp.usuario(authentication))
                .stream()
                .map(DtoConverter::medalha)
                .toList();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public MedalhaResponse criar(
            Authentication authentication,
            @RequestParam("nome") String nome,
            @RequestParam(value = "pontosMin", defaultValue = "0") int pontosMin,
            @RequestPart("imagem") MultipartFile imagem
    ) {
        byte[] bytes;
        try {
            bytes = imagem.getBytes();
        } catch (IOException ex) {
            throw new BusinessRuleException("Não foi possível ler a imagem");
        }
        var medalha = servicoMedalha.criar(
                AuthHttp.usuario(authentication),
                new SalvarMedalhaCommand(
                        nome,
                        pontosMin,
                        bytes,
                        imagem.getContentType(),
                        imagem.getOriginalFilename()
                )
        );
        return new MedalhaResponse(
                DtoConverter.id(medalha.id()),
                medalha.nome(),
                medalha.imagemUrl(),
                medalha.pontosMin(),
                false
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable String id, Authentication authentication) {
        servicoMedalha.excluir(AuthHttp.usuario(authentication), DtoConverter.id(id));
    }

    @PostMapping("/{id}/equipar")
    public UsuarioResponse equipar(@PathVariable String id, Authentication authentication) {
        return DtoConverter.usuario(
                servicoMedalha.equiparAvatar(AuthHttp.usuario(authentication), DtoConverter.id(id))
        );
    }
}
