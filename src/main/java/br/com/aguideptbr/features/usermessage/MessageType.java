package br.com.aguideptbr.features.usermessage;

/**
 * Tipos de mensagem suportados pelo sistema.
 *
 * Armazenado como VARCHAR no banco de dados.
 */
public enum MessageType {
    /**
     * Mensagem de texto simples.
     */
    TEXT,

    /**
     * Mensagem contendo imagem.
     */
    IMAGE,

    /**
     * Mensagem contendo link/URL.
     */
    LINK,

    /**
     * Mensagem contendo vídeo.
     */
    VIDEO,

    /**
     * Mensagem contendo arquivo.
     */
    FILE
}
