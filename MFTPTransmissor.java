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
public class MFTPTransmissor {
    
    // A estrutura do objeto pacote é 105 bytes, logo o conteúdo deve ser de 919 bytes para que o pacote contenha 1024 bytes.
    private static final int TAMANHO_PACOTE_ENVIO = 1024;
    private static final int TAMANHO_PACOTE_RECEBIMENTO = 32;
    private static final int QUANTIDADE_PARTICIPANTES = 1;
    private static final int TIMEOUT = 500;
    
    public static void main(String args[]) throws Exception
    {
        // String mostrada na tela.
        String tela = "+-----------------------------------------------------------------+\n" +
                      "                        MFTP - Transmissor                         \n Arquivo a ser enviado: " + args[0] + "\n";

        // Porta em que o serviço ftp funcionará.
        int porta = 4446;
        
        // IP do do grupo multicast.
        String ipDoGrupo = "230.0.0.1";
        
        // Tipo do tempo de vida, 1 indica que é para rede local, não enviado por roteador.
        int ttl = 1;
        
        // Cria o socket multicast.
        MulticastSocket socket = new MulticastSocket();
        socket.setSoTimeout( TIMEOUT );
        
        // Respostas recebidas.
        int respostasRecebidas = 0;
        
        int numeroDeReceptores;

        // Define a quantidade de receptores.
        if ( args[1] != null )
        {
            numeroDeReceptores = Integer.parseInt(args[1]);
        }
        else
        {
            numeroDeReceptores = QUANTIDADE_PARTICIPANTES;
        }
        
        // Obtém o nome do arquivo que será enviado.
        String nomeArquivo = args[0];
        
        byte[] bufferArquivo = new byte[TAMANHO_PACOTE_ENVIO];
        
        // Abre o arquivo para obter o tamanho em bytes do arquivo.
        File fileTmp = new File(nomeArquivo);
        int tamArquivo = (int) fileTmp.length();
        
        int quantidadeDePacotes = 1;
        
        // Calcula a quantidade de pacotes que serão enviados.
        if ( tamArquivo > TAMANHO_PACOTE_ENVIO )
        {
            quantidadeDePacotes = (int) Math.ceil((double)tamArquivo/TAMANHO_PACOTE_ENVIO); 
        }
        
        // Abre arquivo.
        FileInputStream file = new FileInputStream(nomeArquivo);
        
        String[] conteudoPacote = new String[quantidadeDePacotes];
        
        // Armazena conteúdo conteúdo do arquivo em um vetor de String.
        for ( int i = 0; i < quantidadeDePacotes; i++ )
        {
            // Lê determinada quantidade de bytes do arquivo.
            int quantidadeBytes = file.read(bufferArquivo);
            
            // Caso a quantidade lida for menor que TAMANHO_PACOTE_ENVIO, envia um pacote de menor tamanho.
            if ( quantidadeBytes < TAMANHO_PACOTE_ENVIO )
            {
                byte[] bufferReal = new byte[quantidadeBytes];
                
                for ( int x = 0; x < quantidadeBytes; x++ )
                {
                    bufferReal[x] = bufferArquivo[x];
                }
                
                // Converte array de bytes em String, fazendo um base 64.
                conteudoPacote[i] = Base64.encode(bufferReal);
            }
            else
            {
                // Converte array de bytes em String, fazendo um base 64.
                conteudoPacote[i] = Base64.encode(bufferArquivo);
            }
        }
        
        // Obtém o nome do arquivo sem o path.
        String[] nomeArquivoArray = nomeArquivo.split("/");
        nomeArquivo = nomeArquivoArray[nomeArquivoArray.length -1];
        
        int contador = 1;
        
        // Percorre os pacotes para transmitir.
        while (quantidadeDePacotes >= contador)
        {
            int posicaoPacote = contador - 1;
            
            // Cria pacote com o conteúdo e a identificação.
            Pacote pacote = new Pacote(conteudoPacote[posicaoPacote], contador, quantidadeDePacotes, nomeArquivo);

            // Serializa o objeto pacote em um array de bytes.
            ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
            ObjectOutputStream out = new ObjectOutputStream(bos) ;
            out.writeObject(pacote);
            out.close();

            // Armazena o objeto pacote serializado no vetor de bytes.
            byte[] bufferEnvio = bos.toByteArray();

            // Cria um pacote de datagrama. 
            DatagramPacket pacoteDatagramaEnvio = new DatagramPacket(bufferEnvio, bufferEnvio.length, InetAddress.getByName(ipDoGrupo), porta);

            // Envia o pacote.
            socket.send(pacoteDatagramaEnvio,(byte)ttl);

            byte[] bufferRecebimento = new byte[TAMANHO_PACOTE_RECEBIMENTO];

            try
            {
                // Zera o número de respostas recebidas.
                respostasRecebidas = 0;
                
                // Percorre a quantidade de participantes, deve receber determinada quantidade de respostas.
                for ( int i = 0; i < numeroDeReceptores; i++ )
                {
                    // Cria pacote para recebimento de conteúdo.
                    DatagramPacket pacoteDatagramaRecebido = new DatagramPacket(bufferRecebimento, bufferRecebimento.length);
                    socket.receive(pacoteDatagramaRecebido);
                    
                    // Faz md5 do conteúdo enviado.
                    String md5Enviado = Pacote.fazerMD5(pacote.toString());
                    String md5Recebido = "";
                    
                    // Obtém md5 de resposta do receptor. 
                    if ( md5Enviado.length() != bufferRecebimento.length )
                    {
                         md5Recebido = new String(bufferRecebimento).substring(0, md5Enviado.length());
                    }
                    else
                    {
                        md5Recebido = new String(bufferRecebimento);
                    }
                    
                    // Verifica se md5 recebido do receptor casa com o md5 do conteúdo enviado.
                    if ( md5Enviado.equals(md5Recebido) )
                    {
                        respostasRecebidas++;
                    }
                    else
                    {
                        // Se não fechar o conteúdo do md5, quer dizer que o conteúdo foi corrompido, e tenta enviar novamente.
                        Thread.sleep(TIMEOUT);
                    }
                }
                
                if ( respostasRecebidas == numeroDeReceptores )
                {
                    // Calcula a porcentagem de conclusão de envio.
                    double porcentagemConcluido = Math.ceil(((double) contador/ (double) quantidadeDePacotes) * 100);
                    
                    // Limpa a tela.
                    System.out.print( "\033[H\033[2J" );
                    
                    System.out.println(tela + " Concluído: " + porcentagemConcluido + "%\n+-----------------------------------------------------------------+\n");
                    contador++;
                }
                
            }
            catch ( Exception e )
            {
                // Caso for o último pacote, caso der problema em um receptor, só aguarda mais a resposta do receptor com problema.
                if ( contador == quantidadeDePacotes )
                {
                    numeroDeReceptores = numeroDeReceptores - respostasRecebidas;
                }

            }
        }
        
        // Fecha o socket.
        socket.close();
    }

}
