package tt.projet_tt.Service;

import tt.projet_tt.Entities.User;

import java.util.List;

public interface UserInterface {
    public String login(String email, String password);
    public User register(User user);
    public List<User> getAllUsers();
    public boolean updateUser(Long id, User updatedUser);
    public boolean deleteUser(Long id);
    public boolean addUser(User user);
    public User loginAndReturnUser(String email, String mdp);

}
