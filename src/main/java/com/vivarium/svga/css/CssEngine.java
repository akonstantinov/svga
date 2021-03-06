package com.vivarium.svga.css;

import com.steadystate.css.dom.CSSStyleRuleImpl;
import com.steadystate.css.parser.CSSOMParser;
import org.w3c.css.sac.*;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleSheet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * Created by neurons on 7/30/14.
 */
public class CssEngine {
    private CSSStyleSheet styleSheet = null;
    public void init(String cssText){
        CSSOMParser cssParser = new CSSOMParser();
        InputSource source = new InputSource(
                new InputStreamReader(
                        new ByteArrayInputStream(cssText.getBytes())));
        try {
            styleSheet = cssParser.parseStyleSheet(source, null, null);
        }catch(IOException e){}
    }

    public Map<String,String> getAttributes(String elementName, Map<String, String> attributes){
        if(styleSheet!=null){
            for(int i = 0 ; i < styleSheet.getCssRules().getLength();i++){
                CSSRule rule = styleSheet.getCssRules().item(i);
                switch (rule.getType()){
                    case CSSRule.STYLE_RULE:
                        processStyleRule((CSSStyleRuleImpl)rule, elementName, attributes);
                        break;
                }
            }
        }
        return attributes;
    }

    private void processStyleRule(CSSStyleRuleImpl rule, String elementName, Map<String, String> attributes){
        for( int i = 0 ; i < rule.getSelectors().getLength(); i++){
            Selector selector = rule.getSelectors().item(i);
            boolean satisfy=false;
            switch (selector.getSelectorType()){
                case Selector.SAC_ANY_NODE_SELECTOR:
                    break;
                case Selector.SAC_CONDITIONAL_SELECTOR:
                    satisfy = validateConditionalSelector((ConditionalSelector)selector, elementName, attributes);
                    break;
            }
            if(satisfy){
                CSSStyleDeclaration styleDeclaration = rule.getStyle();
                for (int j = 0; j < styleDeclaration.getLength(); j++) {
                    String property = styleDeclaration.item(j);
                    String value = styleDeclaration.getPropertyCSSValue(property).getCssText();
                    attributes.put(property, transformAttribute(value));
                }
            }
        }
    }

    private boolean validateConditionalSelector(ConditionalSelector selector, String elementName, Map<String, String> attributes) {
        if(selector.getCondition() instanceof AttributeCondition){
            AttributeCondition attrCond = (AttributeCondition) selector.getCondition();
            switch(attrCond.getConditionType()) {
                case Condition.SAC_CLASS_CONDITION:
                    if(attributes.get("class")!=null){
                        String cls[] = attributes.get("class").split(" ");
                        for(int i = 0 ; i < cls.length ; i++){
                            if (attrCond.getValue().equals(cls[i])) {
                                return true;
                            }
                        }
                    }

                    break;
                case Condition.SAC_ID_CONDITION:
                    if (attrCond.getValue().equals(attributes.get("id"))) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

    private String transformAttribute(String attr){
        if(attr.startsWith("rgb(")){
            String[] rgb = attr.substring(4, attr.length() - 1).split(",");
            String red = Integer.toHexString(Integer.parseInt(rgb[0].trim()));
            if(red.length()==1){
                red = "0" + red;
            }
            String green = Integer.toHexString(Integer.parseInt(rgb[1].trim()));
            if(green.length()==1){
                green = "0" + green;
            }
            String blue = Integer.toHexString(Integer.parseInt(rgb[2].trim()));
            if(blue.length()==1){
                blue = "0" + blue;
            }
            return "#" + red + green + blue;
        }
        return attr;
    }

}
