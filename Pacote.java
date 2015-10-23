/*
 * Classe pacote, define a estrutura do pacote.
 * 
 * Tamanho da estrutura: 105 bytes.
 */

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author aluno
 */
public class Pacote implements Serializable {
    
    private String conteudo;
    private int numeroPacote;
    private int quantidadePacotes;
    private String nomeArquivo;
    private String hashMd5;
    
    public Pacote(String conteudo, int numeroPacote, int quantidadePacotes, String nomeArquivo) throws NoSuchAlgorithmException {
        this.conteudo = conteudo;
        this.numeroPacote = numeroPacote;
        this.quantidadePacotes = quantidadePacotes;
        this.nomeArquivo = nomeArquivo;
        this.hashMd5 = fazerMD5(conteudo);
    }

    public String toString() {
        return this.getConteudo();
    }

    /**
     * @return the conteudo
     */
    public String getConteudo() {
        return conteudo;
    }

    /**
     * @param conteudo the conteudo to set
     */
    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    /**
     * @return the numeroPacote
     */
    public int getNumeroPacote() {
        return numeroPacote;
    }

    /**
     * @param numeroPacote the numeroPacote to set
     */
    public void setNumeroPacote(int numeroPacote) {
        this.numeroPacote = numeroPacote;
    }

    /**
     * @return the quantidadePacotes
     */
    public int getQuantidadePacotes() {
        return quantidadePacotes;
    }

    /**
     * @param quantidadePacotes the quantidadePacotes to set
     */
    public void setQuantidadePacotes(int quantidadePacotes) {
        this.quantidadePacotes = quantidadePacotes;
    }

    /**
     * @return the nomeArquivo
     */
    public String getNomeArquivo() {
        return nomeArquivo;
    }

    /**
     * @param nomeArquivo the nomeArquivo to set
     */
    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }
    
    /**
     *  Faz o md5 de um conteúdo.
     * 
     * @param String conteudo Conteúdo a ser criptografado em md5.
     * @return String conteúdo criptografado.
     */
    public static String fazerMD5(String conteudo) throws NoSuchAlgorithmException
    {
        MessageDigest md = MessageDigest.getInstance( "MD5" );  
           
         md.update( conteudo.getBytes() );  
         BigInteger hash = new BigInteger( 1, md.digest() ); 
         
         String hashString = hash.toString( 16 ); 

         return hashString; 
    }

    /**
     * @return the hashMd5
     */
    public String getHashMd5() {
        return hashMd5;
    }

    /**
     * @param hashMd5 the hashMd5 to set
     */
    public void setHashMd5(String hashMd5) {
        this.hashMd5 = hashMd5;
    }
    
}
