package uk.ac.leedsbeckett.finance.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.EntityModel;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.leedsbeckett.finance.exception.UserNotValidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
class UserModelAssemblerTest {
    private User user;

    @Autowired
    private UserModelAssembler userModelAssembler;

    @BeforeEach
    public void setUp() {
        String name = "Walter White";
        String role = "teacher";
        user = new User(name, role);
        Long id = 1L;
        user.setId(id);
    }

    @Test
    void testToModel_withValidUser_ReturnsExpectedEntityModel() {
        EntityModel<User> result = userModelAssembler.toModel(user);
        assertThat(user.equals(result.getContent()));
        assertThat(result.getLinks().hasSize(2));
        assertThat(result.hasLink("http://localhost/users/1"));
        assertThat(result.hasLink("http://localhost/users"));
    }

    @Test
    void testToModel_withIdNull_ThrowsException() {
        user.setId(null);
        assertThrows(UserNotValidException.class, () -> userModelAssembler.toModel(user),
                "Exception was not thrown.");
    }

    @Test
    void testToModel_withIdZero_ThrowsException() {
        user.setId(0L);
        assertThrows(UserNotValidException.class, () -> userModelAssembler.toModel(user),
                "Exception was not thrown.");
    }

    @Test
    void testToModel_withNullArgument_ThrowsException() {
        assertThrows(RuntimeException.class, () -> userModelAssembler.toModel(null),
                "Exception was not thrown.");
    }
}