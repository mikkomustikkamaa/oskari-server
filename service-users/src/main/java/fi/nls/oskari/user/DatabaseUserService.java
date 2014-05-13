package fi.nls.oskari.user;

import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.List;
import java.util.Map;

import org.mindrot.jbcrypt.BCrypt;

public class DatabaseUserService extends UserService {
    private IbatisRoleService roleService = new IbatisRoleService();
    private IbatisUserService userService = new IbatisUserService();

    private static final Logger log = LogFactory.getLogger(DatabaseUserService.class);

    @Override
    public User getGuestUser() {
        User user = super.getGuestUser();
        user.addRole(roleService.findGuestRole());
        return user;
    }

    @Override
    public User login(final String user, final String pass) throws ServiceException {
        try {
            final String hashedPass = "MD5:" + DigestUtils.md5Hex(pass);
            final String username = userService.login(user, hashedPass);
            log.debug("Tried to login user with:", user, "/", pass, "-> ", hashedPass, "- Got username:", username);
            if(username == null) {
                return null;
            }
            return getUser(username);
        }
        catch (Exception ex) {
            throw new ServiceException("Unable to handle login", ex);
        }
    }

    @Override
    public Role[] getRoles(Map<Object, Object> platformSpecificParams) throws ServiceException {
        List<Role> roleList = roleService.findAll();
        return roleList.toArray(new Role[roleList.size()]);
    }

    @Override
    public User getUser(String username) throws ServiceException {
        return userService.findByUserName(username);
    }

    @Override
    public User getUser(long id) throws ServiceException {
        return userService.find(id);
    }

    @Override
    public List<User> getUsers() throws ServiceException {
        return userService.findAll();
    }

    @Override
    public User createUser(User user) throws ServiceException {
        userService.insert(user);
        return userService.findByUserName(user.getScreenname());
    }

    @Override
    public User modifyUser(User user) throws ServiceException {
        userService.update(user);
        return userService.find(user.getId());
    }

    @Override
    public void deleteUser(long id) throws ServiceException {
        userService.delete(id);
    }

    @Override
    public void setUserPassword(long id, String password) throws ServiceException {
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        userService.updatePassword(id, hashed);
    }

}