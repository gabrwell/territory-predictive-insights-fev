package servico;

import dominio.Medicao;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * BÔNUS: exporta o histórico consultado para CSV (separador ';' e vírgula decimal,
 * que o Excel em português abre direto).
 */
public class ExportadorCsv {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    public Path exportar(List<Medicao> medicoes, Path arquivo) throws IOException {
        Path pasta = arquivo.toAbsolutePath().getParent();
        if (pasta != null) {
            Files.createDirectories(pasta);
        }
        try (BufferedWriter escritor = Files.newBufferedWriter(arquivo, StandardCharsets.UTF_8)) {
            escritor.write('﻿'); // BOM: faz o Excel reconhecer os acentos
            escritor.write("id;sensor;tipo;valor;unidade;data_hora");
            escritor.newLine();
            for (Medicao m : medicoes) {
                escritor.write(String.format(PT_BR, "%d;%s;%s;%.2f;%s;%s",
                        m.getId(), m.getSensorId(), m.getTipo().getDescricao(), m.getValor(),
                        m.getTipo().getUnidade(), m.getDataHora().format(Medicao.FORMATO_DATA)));
                escritor.newLine();
            }
        }
        return arquivo.toAbsolutePath();
    }
}
