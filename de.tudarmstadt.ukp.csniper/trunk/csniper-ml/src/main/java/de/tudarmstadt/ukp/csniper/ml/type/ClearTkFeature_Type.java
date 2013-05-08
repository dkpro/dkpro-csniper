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
/* First created by JCasGen Wed Sep 05 02:36:54 CEST 2012 */
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

/** This annotation saves a ClearTK Feature.
 * Updated by JCasGen Sat Apr 20 16:04:07 CEST 2013
 * @generated */
public class ClearTkFeature_Type extends Annotation_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (ClearTkFeature_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = ClearTkFeature_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new ClearTkFeature(addr, ClearTkFeature_Type.this);
  			   ClearTkFeature_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new ClearTkFeature(addr, ClearTkFeature_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = ClearTkFeature.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.csniper.ml.type.ClearTkFeature");
 
  /** @generated */
  final Feature casFeat_clearTkFeatureName;
  /** @generated */
  final int     casFeatCode_clearTkFeatureName;
  /** @generated */ 
  public String getClearTkFeatureName(int addr) {
        if (featOkTst && casFeat_clearTkFeatureName == null)
      jcas.throwFeatMissing("clearTkFeatureName", "de.tudarmstadt.ukp.csniper.ml.type.ClearTkFeature");
    return ll_cas.ll_getStringValue(addr, casFeatCode_clearTkFeatureName);
  }
  /** @generated */    
  public void setClearTkFeatureName(int addr, String v) {
        if (featOkTst && casFeat_clearTkFeatureName == null)
      jcas.throwFeatMissing("clearTkFeatureName", "de.tudarmstadt.ukp.csniper.ml.type.ClearTkFeature");
    ll_cas.ll_setStringValue(addr, casFeatCode_clearTkFeatureName, v);}
    
  
 
  /** @generated */
  final Feature casFeat_clearTkFeatureValue;
  /** @generated */
  final int     casFeatCode_clearTkFeatureValue;
  /** @generated */ 
  public String getClearTkFeatureValue(int addr) {
        if (featOkTst && casFeat_clearTkFeatureValue == null)
      jcas.throwFeatMissing("clearTkFeatureValue", "de.tudarmstadt.ukp.csniper.ml.type.ClearTkFeature");
    return ll_cas.ll_getStringValue(addr, casFeatCode_clearTkFeatureValue);
  }
  /** @generated */    
  public void setClearTkFeatureValue(int addr, String v) {
        if (featOkTst && casFeat_clearTkFeatureValue == null)
      jcas.throwFeatMissing("clearTkFeatureValue", "de.tudarmstadt.ukp.csniper.ml.type.ClearTkFeature");
    ll_cas.ll_setStringValue(addr, casFeatCode_clearTkFeatureValue, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public ClearTkFeature_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_clearTkFeatureName = jcas.getRequiredFeatureDE(casType, "clearTkFeatureName", "uima.cas.String", featOkTst);
    casFeatCode_clearTkFeatureName  = (null == casFeat_clearTkFeatureName) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_clearTkFeatureName).getCode();

 
    casFeat_clearTkFeatureValue = jcas.getRequiredFeatureDE(casType, "clearTkFeatureValue", "uima.cas.String", featOkTst);
    casFeatCode_clearTkFeatureValue  = (null == casFeat_clearTkFeatureValue) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_clearTkFeatureValue).getCode();

  }
}



    
