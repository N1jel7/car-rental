package com.innowise.carrental.service;

import com.innowise.carrental.dao.UserDao;
import com.innowise.carrental.entity.Role;
import com.innowise.carrental.entity.User;
import com.innowise.carrental.exception.DaoException;
import com.innowise.carrental.exception.ServiceException;
import com.innowise.carrental.exception.ValidationException;
import com.innowise.carrental.util.PasswordUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    private UserService userService;

    private UserService service() {
        return new UserService(userDao);
    }

    @Test
    void register_validData_savesUserWithHashedPassword() throws Exception {
        // given
        userService = service();
        when(userDao.findByEmail("new@carrental.com")).thenReturn(Optional.empty());

        // when
        User result = userService.register("new@carrental.com", "password123", "Ivan Ivanov", "+375291234567");

        // then
        assertEquals("new@carrental.com", result.getEmail());
        assertEquals(Role.USER, result.getRole());
        assertTrue(PasswordUtil.verify("password123", result.getPasswordHash()));
        verify(userDao, times(1)).save(result);
    }

    @Test
    void register_emailAlreadyRegistered_throwsValidationExceptionAndDoesNotSave() throws Exception {
        // given
        userService = service();
        when(userDao.findByEmail("taken@carrental.com"))
                .thenReturn(Optional.of(new User()));

        // when / then
        assertThrows(ValidationException.class, () ->
                userService.register("taken@carrental.com", "password123", "Ivan Ivanov", null));
        verify(userDao, never()).save(any());
    }

    @Test
    void register_blankFullName_throwsValidationExceptionBeforeTouchingDao() {
        // given
        userService = service();

        // when / then
        assertThrows(ValidationException.class, () ->
                userService.register("new@carrental.com", "password123", " ", null));
    }

    @Test
    void login_correctCredentials_returnsUser() throws Exception {
        // given
        userService = service();
        User stored = User.builder()
                .email("user@carrental.com")
                .passwordHash(PasswordUtil.hash("password123"))
                .fullName("Ivan Ivanov")
                .role(Role.USER)
                .build();
        when(userDao.findByEmail("user@carrental.com")).thenReturn(Optional.of(stored));

        // when
        User result = userService.login("user@carrental.com", "password123");

        // then
        assertEquals(stored, result);
    }

    @Test
    void login_wrongPassword_throwsValidationException() throws Exception {
        // given
        userService = service();
        User stored = User.builder()
                .email("user@carrental.com")
                .passwordHash(PasswordUtil.hash("password123"))
                .role(Role.USER)
                .build();
        when(userDao.findByEmail("user@carrental.com")).thenReturn(Optional.of(stored));

        // when / then
        assertThrows(ValidationException.class, () ->
                userService.login("user@carrental.com", "wrongPassword"));
    }

    @Test
    void login_unknownEmail_throwsValidationException() throws Exception {
        // given
        userService = service();
        when(userDao.findByEmail("ghost@carrental.com")).thenReturn(Optional.empty());

        // when / then
        assertThrows(ValidationException.class, () ->
                userService.login("ghost@carrental.com", "password123"));
    }

    @Test
    void changePassword_wrongOldPassword_throwsValidationExceptionAndDoesNotUpdate() throws Exception {
        // given
        userService = service();
        User stored = User.builder()
                .passwordHash(PasswordUtil.hash("correctOldPassword"))
                .build();
        when(userDao.findById(1L)).thenReturn(Optional.of(stored));

        // when / then
        assertThrows(ValidationException.class, () ->
                userService.changePassword(1L, "wrongOldPassword", "newPassword123"));
        verify(userDao, never()).update(any());
    }

    @Test
    void changePassword_correctOldPassword_updatesHash() throws Exception {
        // given
        userService = service();
        User stored = User.builder()
                .passwordHash(PasswordUtil.hash("correctOldPassword"))
                .build();
        when(userDao.findById(1L)).thenReturn(Optional.of(stored));

        // when
        userService.changePassword(1L, "correctOldPassword", "newPassword123");

        // then
        assertTrue(PasswordUtil.verify("newPassword123", stored.getPasswordHash()));
        verify(userDao).update(stored);
    }

    @Test
    void findById_userDoesNotExist_throwsServiceException() throws Exception {
        // given
        userService = service();
        when(userDao.findById(42L)).thenReturn(Optional.empty());

        // when / then
        assertThrows(ServiceException.class, () -> userService.findById(42L));
    }

    @Test
    void findById_daoFails_wrapsExceptionAsServiceException() throws Exception {
        // given
        userService = service();
        when(userDao.findById(1L)).thenThrow(new DaoException("connection lost"));

        // when / then
        assertThrows(ServiceException.class, () -> userService.findById(1L));
    }

}
