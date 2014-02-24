import ar.com.fdvs.dj.domain.DynamicReport
import ar.com.fdvs.dj.domain.DynamicReportOptions

/**
 * @author Alejandro Gomez (alejandro.gomez@fdvsolutions.com)
 * Date: 14-dic-2008
 * Time: 17:21:07
 */

class DynamicReportFactory extends AbstractFactory {

    public Object newInstance(FactoryBuilderSupport factoryBuilderSupport, Object o, Object o1, Map map) {
        def report = new DynamicReport()
        report.options = new DynamicReportOptions()
        report
    }
}
