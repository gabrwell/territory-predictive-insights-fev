package servico;

import dominio.NivelAlerta;

import java.time.LocalDateTime;
import java.util.Optional;

/** Relatório de status completo (montado pelo RelatorioBuilder). Imutável. */
public final class Relatorio {

    private final LocalDateTime geradoEm;
    private final NivelAlerta nivelMaisGrave;
    private final String conteudo;

    Relatorio(LocalDateTime geradoEm, NivelAlerta nivelMaisGrave, String conteudo) {
        this.geradoEm = geradoEm;
        this.nivelMaisGrave = nivelMaisGrave;
        this.conteudo = conteudo;
    }

    public LocalDateTime getGeradoEm() {
        return geradoEm;
    }

    public Optional<NivelAlerta> getNivelMaisGrave() {
        return Optional.ofNullable(nivelMaisGrave);
    }

    public String getConteudo() {
        return conteudo;
    }

    @Override
    public String toString() {
        return conteudo;
    }
}
