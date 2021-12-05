package uk.ac.leedsbeckett.finance.system;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Inherit database interaction functionality from JpaRepository for User class, of ID type Long
 * Create new Users *
 * Update existing Users *
 * Delete Users *
 * Find Users (one, all, or search by simple or complex properties)
 */
public interface UserRepository extends JpaRepository<User, Long> {

}
