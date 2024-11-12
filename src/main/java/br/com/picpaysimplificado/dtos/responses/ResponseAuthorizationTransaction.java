package br.com.picpaysimplificado.dtos.responses;

// Objeto response(o que vem como resposta no corpo/body) da requisição que solicita autorização de transferência
// mock - AuthorizationTransaction
public class ResponseAuthorizationTransaction {

    public String status;
    public Data data;

    public static class Data {
        public boolean authorization;
    }
}
