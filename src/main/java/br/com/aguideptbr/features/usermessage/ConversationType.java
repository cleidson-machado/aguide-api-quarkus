package br.com.aguideptbr.features.usermessage;

/**
 * Tipos de conversa suportados pelo sistema.
 *
 * Armazenado como VARCHAR no banco de dados.
 */
public enum ConversationType {
    /**
     * Conversa direta entre dois usuários (1-1).
     * Não possui nome de grupo, apenas participantes.
     */
    DIRECT,

    /**
     * Conversa em grupo (múltiplos usuários).
     * Possui nome, ícone, lista de participantes.
     */
    GROUP,

    /**
     * Canal público (broadcast).
     * Apenas administradores podem enviar mensagens.
     */
    CHANNEL
}
