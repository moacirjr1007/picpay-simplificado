package br.com.picpaysimplificado.controllers;

import br.com.picpaysimplificado.entitys.user.User;
import br.com.picpaysimplificado.services.UserService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

// "/users" - rota do controller (O controller tem basicamente a funcionalidade de chamar métodos no servidor)
// Métodos do tipo POST, PUT e DELETE precisam ser anotados com @Transactional, pois alteram dados do banco de dados
// Eu coloquei o @Transactional nos métodos chamados pelos métodos POST - veja nos Package services!
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserController {

    // Injeta dependências
    // O Quarkus, durante a execução, se encarrega de fornecer uma instância de UserService pronta para uso.
    @Inject
    UserService userService;

    // O verbo GET na rota /users retorna a lista de Usuários do sistema
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> getUsers() {
        return userService.listUsers();
    }

    // O verbo POST na rota /users deve receber em seu body um JSON da entity User
    // O methode cria o usuário no sistema seguindo às regras definidas na entity
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser (User newUser) {
        User createUser = userService.saveUser(newUser);
        return Response.status(Response.Status.CREATED).entity(createUser).build();
    }

    // Novo método DELETE
    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") Long id) {
        try {
            userService.deleteUser(id);
            return Response.status(Response.Status.NO_CONTENT).build(); // 204 No Content (usuário deletado)
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build(); // 400 Bad Request se algo der errado
        }
    }
}
