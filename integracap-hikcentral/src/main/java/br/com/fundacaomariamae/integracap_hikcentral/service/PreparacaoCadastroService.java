package br.com.fundacaomariamae.integracap_hikcentral.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import br.com.fundacaomariamae.integracap_hikcentral.dto.CadastroEntrada;

@Service
public class PreparacaoCadastroService {
    private final int maxBytes;
    private final long maxPixels;
    public record Foto(String tipo, int tamanhoBytes, int largura, int altura, String sha256, String base64) {}
    public record Preparado(String estado, String contrato, String codigoOrigem, String nome,
                            String codigoOrganizacao, Foto foto) {}

    public PreparacaoCadastroService(
            @Value("${hikcentral.preparacao.max-foto-bytes:2097152}") int maxBytes,
            @Value("${hikcentral.preparacao.max-pixels:16000000}") long maxPixels) {
        if (maxBytes <= 0 || maxPixels <= 0) throw new IllegalArgumentException("Limites devem ser positivos");
        this.maxBytes = maxBytes;
        this.maxPixels = maxPixels;
    }

    public Preparado preparar(CadastroEntrada cadastro, byte[] bytes) {
        if (cadastro == null) throw new IllegalArgumentException("Cadastro obrigatorio");
        String codigo = normalizar(cadastro.codigoOrigem(), 128);
        String nome = normalizar(cadastro.nome(), 256);
        String org = normalizar(cadastro.codigoOrganizacao(), 128);
        if (bytes == null || bytes.length == 0 || bytes.length > maxBytes)
            throw new IllegalArgumentException("Foto vazia ou acima do limite local de bytes");
        try (var input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IllegalArgumentException("Foto deve ser JPEG ou PNG legivel");
            var reader = readers.next();
            try {
                reader.setInput(input);
                String formato = reader.getFormatName().toLowerCase(java.util.Locale.ROOT);
                if (!formato.equals("jpeg") && !formato.equals("png"))
                    throw new IllegalArgumentException("Formato local permitido: JPEG ou PNG");
                int largura = reader.getWidth(0), altura = reader.getHeight(0);
                if (largura <= 0 || altura <= 0 || (long) largura * altura > maxPixels)
                    throw new IllegalArgumentException("Foto acima do limite local de pixels");
                if (reader.read(0) == null) throw new IllegalArgumentException("Foto ilegivel");
                String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
                Foto foto = new Foto("image/" + formato, bytes.length, largura, altura,
                        hash, Base64.getEncoder().encodeToString(bytes));
                return new Preparado("PREPARADO_LOCALMENTE", "INTERNO_PROVISORIO", codigo, nome, org, foto);
            } finally { reader.dispose(); }
        } catch (IOException e) {
            throw new IllegalArgumentException("Foto ilegivel", e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String normalizar(String valor, int max) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException("Campos do cadastro sao obrigatorios");
        String resultado = valor.strip();
        if (resultado.length() > max || resultado.codePoints().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException("Campo acima do limite local ou com caracteres de controle");
        return resultado;
    }
}
