/*******************************************************************************
 * Copyright 2013
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/* First created by JCasGen Mon May 07 13:31:09 CEST 2012 */
package de.tudarmstadt.ukp.csniper.ml.type;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** This type is intended to be used with a binary classifier.
It contains fields for
- gold value
- predicted value
- score used for prediction
- category which is being predicted
 * Updated by JCasGen Sat Apr 20 16:04:12 CEST 2013
 * @generated */
public class BooleanClassification_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (BooleanClassification_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = BooleanClassification_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new BooleanClassification(addr, BooleanClassification_Type.this);
  			   BooleanClassification_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new BooleanClassification(addr, BooleanClassification_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = BooleanClassification.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.csniper.ml.type.BooleanClassification");
 
  /** @generated */
  final Feature casFeat_expectedLabel;
  /** @generated */
  final int     casFeatCode_expectedLabel;
  /** @generated */ 
  public boolean getExpectedLabel(int addr) {
        if (featOkTst && casFeat_expectedLabel == null)
      jcas.throwFeatMissing("expectedLabel", "de.tudarmstadt.ukp.csniper.ml.type.BooleanClassification");
    return ll_cas.ll_getBooleanValue(addr, casFeatCode_expectedLabel);
  }
  /** @generated */    
  public void setExpectedLabel(int addr, boolean v) {
        if (featOkTst && casFeat_expectedLabel == null)
      jcas.throwFeatMissing("expectedLabel", "de.tudarmstadt.ukp.csniper.ml.type.BooleanClassification");
    ll_cas.ll_setBooleanValue(addr, casFeatCode_expectedLabel, v);}
    
  
 
  /** @generated */
  final Feature casFeat_predictedLabel;
  /** @generated */
  final int     casFeatCode_predictedLabel;
  /** @generated */ 
  public boolean getPredictedLabel(int addr) {
        if (featOkTst && casFeat_predictedLabel == null)
      jcas.throwFeatMissing("predictedLabel", "de.tudarmstadt.ukp.csniper.ml.type.BooleanClassification");
    return ll_cas.ll_getBooleanValue(addr, casFeatCode_predictedLabel);
  }
  /** @generated */    
  public void setPredictedLabel(int addr, boolean v) {
        if (featOkTst && casFeat_predictedLabel == null)
      jcas.throwFeatMissing("predictedLabel", "de.tudarmstadt.ukp.csniper.ml.type.BooleanClassification");
    ll_cas.ll_setBooleanValue(addr, casFeatCode_predictedLabel, v);}
    
  
 
  /** @generated */
  final Feature casFeat_score;
  /** @generated */
  final int     casFeatCode_score;
  /** @generated */ 
  public double getScore(int addr) {
        if (featOkTst && casFeat_score == null)
      jcas.throwFeatMissing("score", "de.tudarmstadt.ukp.csniper.ml.type.BooleanClassification");
    return ll_cas.ll_getDoubleValue(addr, casFeatCode_score);
  }
  /** @generated */    
  public void setScore(int addr, double v) {
        if (featOkTst && casFeat_score == null)
      jcas.throwFeatMissing("score", "de.tudarmstadt.ukp.csniper.ml.type.BooleanClassification");
    ll_cas.ll_setDoubleValue(addr, casFeatCode_score, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public BooleanClassification_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_expectedLabel = jcas.getRequiredFeatureDE(casType, "expectedLabel", "uima.cas.Boolean", featOkTst);
    casFeatCode_expectedLabel  = (null == casFeat_expectedLabel) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_expectedLabel).getCode();

 
    casFeat_predictedLabel = jcas.getRequiredFeatureDE(casType, "predictedLabel", "uima.cas.Boolean", featOkTst);
    casFeatCode_predictedLabel  = (null == casFeat_predictedLabel) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_predictedLabel).getCode();

 
    casFeat_score = jcas.getRequiredFeatureDE(casType, "score", "uima.cas.Double", featOkTst);
    casFeatCode_score  = (null == casFeat_score) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_score).getCode();

  }
}



    
