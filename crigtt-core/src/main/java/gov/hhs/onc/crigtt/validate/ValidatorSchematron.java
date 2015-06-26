package gov.hhs.onc.crigtt.validate;

import gov.hhs.onc.crigtt.beans.NamedBean;
import gov.hhs.onc.crigtt.xml.impl.XdmDocument;
import java.util.List;
import java.util.Map;
import javax.xml.transform.Source;
import net.sf.saxon.s9api.XsltExecutable;
import org.springframework.beans.factory.InitializingBean;

public interface ValidatorSchematron extends NamedBean, InitializingBean {
    public XdmDocument transform(Source docSrc) throws Exception;

    public Map<String, ValidatorAssertion> getActiveAssertions();

    public Map<String, ValidatorCode> getActiveCodes();

    public Map<String, ValidatorCodeSystem> getActiveCodeSystems();

    public Map<String, ValidatorPattern> getActivePatterns();

    public Map<String, ValidatorPhase> getActivePhases();

    public Map<String, ValidatorRule> getActiveRules();

    public ValidatorSchema getActiveSchema();

    public Map<String, ValidatorValueSet> getActiveValueSets();

    public ThreadLocal<Map<Object, Object>> getContextDataThreadLocal();

    public Map<String, String> getInitialTestExpressions();

    public Map<String, ?> getParameters();

    public void setParameters(Map<String, ?> params);

    public Map<String, String> getPatternPhases();

    public String getQueryBinding();

    public void setQueryBinding(String queryBinding);

    public XdmDocument[] getReferencedDocuments();

    public void setReferencedDocuments(XdmDocument ... referencedDocs);

    public String getSchemaVersion();

    public void setSchemaVersion(String schemaVersion);

    public Source getSource();

    public void setSource(Source src);

    public XdmDocument getStaticVocabDocument();

    public void setStaticVocabDocument(XdmDocument staticVocabDoc);

    public Map<String, List<String>> getValueSetCodes();

    public XsltExecutable[] getXsltExecutables();

    public void setXsltExecutables(XsltExecutable ... xsltExecs);
}