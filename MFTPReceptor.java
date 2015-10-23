/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author aluno
 */
public class MFTPReceptor {

    private static final int TAMANHO_PACOTE_ENVIO = 2048;
    private static final int TAMANHO_PACOTE_RECEBIMENTO = 32;

    public static void main(String args[]) throws Exception
    {
        // String mostrada na tela.
        String tela = "+-----------------------------------------------------------------+\n" +
                      "                        MFTP - Receptor                            \n Arquivo a ser recebido: ";
        
        // Porta em que o serviço ftp funcionará.
        int porta = 4446;
        
        // IP do do grupo multicast.
        String ipDoGrupo = "230.0.0.1";
        
        // Armazena todo conteúdo recibo.
        String conteudoCompleto = "";
        
        // Armazena o pacote atual recebido.
        int numeroPacoteAtual = 0;
        
        // Cria o socket multicast.
        MulticastSocket socket = new MulticastSocket(porta);
        
        // Conecta no grupo multicast.
        socket.joinGroup(InetAddress.getByName(ipDoGrupo));
        
        // Cria um pacote de datagrmaa.
        byte buffer[] = new byte[TAMANHO_PACOTE_ENVIO];
        
        // Cria pacote para enviar ok de recimento.
        byte bufferEnvio[] = new byte[TAMANHO_PACOTE_RECEBIMENTO];
        
        String md5DoPacote = "";
        
        FileOutputStream file = null;
        
        System.out.println("\nIniciando recebimento de arquivo...\n");
        
        // Recebe o pacote enviado pelo servidor.
        while ( true )
        {
            // Cria pacote para recebimento de conteúdo.
            DatagramPacket pacoteDatagramaRecebido = new DatagramPacket(buffer, buffer.length);
            socket.receive(pacoteDatagramaRecebido);

            // Deserializa o pacote
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer));
            Pacote pacote = (Pacote)in.readObject();
            in.close();
            
            //Verifica se receptor pode receber a transmissão do arquivo (em caso de receptor se escrever após a transmissão ter iniciada).
            if ( (pacote.getNumeroPacote() - numeroPacoteAtual) > 1 )
            {
                break;
            }
            
            // Converte a String em bytes.
            bufferEnvio = Pacote.fazerMD5(pacote.toString()).getBytes();
        
            // Cria pacote para ok de recibmento.
            DatagramPacket pacoteDatagramaEnvio = new DatagramPacket(bufferEnvio, bufferEnvio.length, pacoteDatagramaRecebido.getAddress(), pacoteDatagramaRecebido.getPort());
    
            // Confirma recebimento, vericando o hash md5.
            if ( Pacote.fazerMD5(pacote.toString()).equals(pacote.getHashMd5()) )
            {
                // Confirma recebimento para o transmissor.
                socket.send(pacoteDatagramaEnvio);
            
                // Verifica qual é o pacote recibo, caso for um novo, obtém o conteúdo.
                if ( (pacote.getNumeroPacote() > numeroPacoteAtual)  )
                {
                    numeroPacoteAtual = pacote.getNumeroPacote();
                    
                    // Transforma o conteúdo do pacote em array de bytes fazendo o decode do base 64.
                    byte[] conteudoRecebido = Base64.decode(pacote.getConteudo());
                    
                    // Cria o arquivo de saída.
                    if ( file == null )
                    {
                        file = new FileOutputStream(pacote.getNomeArquivo());
                        tela += pacote.getNomeArquivo() + "\n";
                    }
                    
                    // Limpa tela.
                    System.out.print( "\033[H\033[2J" );

                    // Calcula a porcentagem de recebimento.
                    double porcentagemConcluido = Math.ceil(((double) pacote.getNumeroPacote() / (double) pacote.getQuantidadePacotes()) * 100);

                    // Imprime porcentagem de conclusão.
                    System.out.println(tela + " Concluído: " + porcentagemConcluido + "%\n+-----------------------------------------------------------------+\n");
                    
                    // Grava o conteúdo do pacote no arquivo.
                    file.write(conteudoRecebido, 0, conteudoRecebido.length);
                }
                
                // Verifica se chegou a quantidade certa de pacotes, caso sim, sai do while e sai do grupo.
                if ( pacote.getNumeroPacote() == pacote.getQuantidadePacotes() )
                {
                    break;
                }
            }
        }
        
        if ( file != null )
        {
            // Fecha o arquivo.
            file.close();
        }
        
        // Ao terminar a recepção, sai do grupo.
        socket.leaveGroup(InetAddress.getByName(ipDoGrupo));
        socket.close();
    }
}
