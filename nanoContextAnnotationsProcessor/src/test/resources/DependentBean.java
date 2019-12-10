import org.nanocontext.annotations.NanoBean;
import org.nanocontext.annotations.NanoInject;

@NanoBean (identifier = "dependent-baen")
public class DependentBean {
    private final IndependentBean ib;
    public DependentBean(@NanoInject(identifier = "independent-bean") final IndependentBean ib) {
        this.ib = ib;
    }

    public IndependentBean getIb() {
        return ib;
    }
}
