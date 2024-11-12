package br.com.picpaysimplificado.services;

import br.com.picpaysimplificado.dtos.requests.RequestSendNotification;
import br.com.picpaysimplificado.dtos.requests.RequestTransaction;
import br.com.picpaysimplificado.dtos.responses.ResponseSendNotification;
import br.com.picpaysimplificado.entitys.transaction.Transaction;
import br.com.picpaysimplificado.entitys.user.User;
import br.com.picpaysimplificado.mocks.SendNotification.SendNotification;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import br.com.picpaysimplificado.dtos.responses.ResponseAuthorizationTransaction;
import br.com.picpaysimplificado.mocks.AuthorizationTransaction.AuthorizationTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.math.BigDecimal;

@ApplicationScoped
public class TransactionService {

    // Injeta dependências
    // O Quarkus, durante a execução, se encarrega de fornecer uma instância de UserService pronta para uso.
    @Inject
    UserService userService;

    // Transactional pois o methode altera dados no banco de dados!
    @Transactional
    public Transaction createTransaction(RequestTransaction requestTransaction) throws  Exception{

        // Validações das regras de negócio:
        BigDecimal amount = requestTransaction.amount;
        User sender = userService.findUserSenderById(requestTransaction.senderId);
        User receiver = userService.findUserReceiverById(requestTransaction.receiverId);
        userService.validadeTransaction(sender, requestTransaction.amount);

        // Validação por autorização externa do Mock:
        authorizeTransaction();

        // Passando por todas às autorizações (não ocorreu nenhuma Exceptions) a transferência é realizada:
        sender.balance = sender.balance.subtract(requestTransaction.amount);
        receiver.balance = receiver.balance.add(requestTransaction.amount);

        // Persiste a transferência:
        Transaction transaction = new Transaction();

        transaction.amount = amount;
        transaction.sender = sender;
        transaction.receiver = receiver;
        transaction.notifySender = sendNotificationEmail();
        transaction.notifyReceiver = sendNotificationEmail();

        userService.saveUser(sender);
        userService.saveUser(receiver);
        transaction.persist();

        return transaction;
    }

    // Injeta dependências
    // O Quarkus, durante a execução, se encarrega de fornecer uma instância de AuthorizationTransaction pronta para uso.
    @Inject
    @RestClient
    AuthorizationTransaction authorizationTransaction;

    // MOCK de Autorização da transação
    // Se a autorização falhar uma Exception é lançada
    public void authorizeTransaction() throws Exception {
        try {
            ResponseAuthorizationTransaction response = authorizationTransaction.authorize();
        } catch (ClientWebApplicationException err) {
            throw new Exception("Transferência recusada pelo serviço de autorização");
        }
    }

    // Injeta dependências
    // O Quarkus, durante a execução, se encarrega de fornecer uma instância de AuthorizationTransaction pronta para uso.
    @Inject
    @RestClient
    SendNotification sendNotification;

    // MOCK que envia notificação
    // Retorna o feedback da notificação (Sucesso ou Falha)
    public String sendNotificationEmail() {
        String notifyStatus;
        RequestSendNotification requestSendNotification = new RequestSendNotification();

        try {
            ResponseSendNotification response = sendNotification.sendNotification(requestSendNotification);
            return "Notificação enviada com sucesso";
        } catch (ClientWebApplicationException err) {
            return "Falha ao enviar a notificação";
        }
    }
}
