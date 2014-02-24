import ar.com.fdvs.dj.domain.entities.columns.SimpleColumn
import ar.com.fdvs.dj.domain.ColumnProperty
import ar.com.fdvs.dj.domain.Style
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn
import ar.com.fdvs.dj.domain.DynamicReport
import groovy.util.ObjectGraphBuilder.DefaultClassNameResolver

/**
 * @author Alejandro Gomez (alejandro.gomez@fdvsolutions.com)
 * Date: 14-dic-2008
 * Time: 17:19:49
 */

class DynamicJasperBuilder extends ObjectGraphBuilder {

    private lastNodeName

    def DynamicJasperBuilder() {
        registerFactory("dynamicReport", new DynamicReportFactory())
        registerBeanFactory("column", SimpleColumn)
        registerBeanFactory("columnProperty", ColumnProperty)
        registerBeanFactory("style", Style)
        registerBeanFactory("headerStyle", Style)
        classNameResolver = new DynamicJasperClassNameResolver()
    }

    protected Object createNode(Object name, Map attributes, Object value) {
        lastNodeName = name
        super.createNode name, attributes, value
    }

    protected void setParent(def parent, def child) {
        if (child instanceof AbstractColumn) {
            parent.columns << child
        } else {
            //super.setParent(parent, child)
            parent[lastNodeName] = child
        }
    }

}

class DynamicJasperClassNameResolver extends DefaultClassNameResolver {

    public String resolveClassname(String classname) {
        switch (classname) {
            case 'title':
                return 'java.lang.String'
            case 'subtitle':
                return 'java.lang.String'
            default:
                return super.resolveClassname(classname)
        }
    }
}