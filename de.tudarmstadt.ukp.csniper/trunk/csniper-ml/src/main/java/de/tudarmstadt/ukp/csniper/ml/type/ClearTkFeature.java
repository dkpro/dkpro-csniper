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

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;


/** This annotation saves a ClearTK Feature.
 * Updated by JCasGen Sat Apr 20 16:04:07 CEST 2013
 * XML source: D:/workspace/de.tudarmstadt.ukp.csniper/csniper-ml/src/main/resources/desc/type/ClearTkFeature.xml
 * @generated */
public class ClearTkFeature extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(ClearTkFeature.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated  */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected ClearTkFeature() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public ClearTkFeature(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public ClearTkFeature(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public ClearTkFeature(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: clearTkFeatureName

  /** getter for clearTkFeatureName - gets The name of the ClearTK feature.
   * @generated */
  public String getClearTkFeatureName() {
    if (ClearTkFeature_Type.featOkTst && ((ClearTkFeature_Type)jcasType).casFeat_clearTkFeatureName == null)
      jcasType.jcas.throwFeatMissing("clearTkFeatureName", "de.tudarmstadt.ukp.csniper.ml.type.ClearTkFeature");
    return jcasType.ll_cas.ll_getStringValue(addr, ((ClearTkFeature_Type)jcasType).casFeatCode_clearTkFeatureName);}
    
  /** setter for clearTkFeatureName - sets The name of the ClearTK feature. 
   * @generated */
  public void setClearTkFeatureName(String v) {
    if (ClearTkFeature_Type.featOkTst && ((ClearTkFeature_Type)jcasType).casFeat_clearTkFeatureName == null)
      jcasType.jcas.throwFeatMissing("clearTkFeatureName", "de.tudarmstadt.ukp.csniper.ml.type.ClearTkFeature");
    jcasType.ll_cas.ll_setStringValue(addr, ((ClearTkFeature_Type)jcasType).casFeatCode_clearTkFeatureName, v);}    
   
    
  //*--------------*
  //* Feature: clearTkFeatureValue

  /** getter for clearTkFeatureValue - gets The value of the ClearTK feature.
   * @generated */
  public String getClearTkFeatureValue() {
    if (ClearTkFeature_Type.featOkTst && ((ClearTkFeature_Type)jcasType).casFeat_clearTkFeatureValue == null)
      jcasType.jcas.throwFeatMissing("clearTkFeatureValue", "de.tudarmstadt.ukp.csniper.ml.type.ClearTkFeature");
    return jcasType.ll_cas.ll_getStringValue(addr, ((ClearTkFeature_Type)jcasType).casFeatCode_clearTkFeatureValue);}
    
  /** setter for clearTkFeatureValue - sets The value of the ClearTK feature. 
   * @generated */
  public void setClearTkFeatureValue(String v) {
    if (ClearTkFeature_Type.featOkTst && ((ClearTkFeature_Type)jcasType).casFeat_clearTkFeatureValue == null)
      jcasType.jcas.throwFeatMissing("clearTkFeatureValue", "de.tudarmstadt.ukp.csniper.ml.type.ClearTkFeature");
    jcasType.ll_cas.ll_setStringValue(addr, ((ClearTkFeature_Type)jcasType).casFeatCode_clearTkFeatureValue, v);}    
  }

    
