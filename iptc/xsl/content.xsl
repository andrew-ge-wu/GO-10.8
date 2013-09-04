<?xml version="1.0" encoding="UTF-8"?>
  <!-- 
Codes downloaded from http://cv.iptc.org/Requester?human=0&scheme=subjectcode 
Remove all attributes in the <knowledgeItem> tag.
-->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="/">
    <batch xmlns="http://www.polopoly.com/polopoly/cm/xmlio">
      <batch xmlns="http://www.polopoly.com/polopoly/cm/xmlio">
        <xsl:for-each select="//knowledgeItem/conceptSet/concept/conceptId[ends-with(@qcode, '000000')]/..">
          <content xmlns="http://www.polopoly.com/polopoly/cm/xmlio">
            <metadata>
              <contentid>
                <major>Department</major>
                <externalid>
                  <xsl:value-of select="replace(conceptId/@qcode,'(subj):([0-9][0-9]+?)(000)*$','$1ect-$2')" />
                </externalid>
              </contentid>
              <input-template>
                <externalid>p.TreeCategory</externalid>
              </input-template>
              <security-parent>
                <externalid>department.categorydimension.subject</externalid>
              </security-parent>
            </metadata>
            <component group="polopoly.Content" name="name">
              <xsl:value-of select="./name[@xml:lang='en-GB']" />
            </component>
          </content>
        </xsl:for-each>
      </batch>
      <batch xmlns="http://www.polopoly.com/polopoly/cm/xmlio">
        <xsl:for-each select="//knowledgeItem/conceptSet/concept/conceptId[ends-with(@qcode, '000')]/..">
          <xsl:if test="./broader[ends-with(@qcode, '000000')]">
            <content xmlns="http://www.polopoly.com/polopoly/cm/xmlio">
              <metadata>
                <contentid>
                  <major>Department</major>
                  <externalid>
                    <xsl:value-of select="replace(conceptId/@qcode,'(subj):([0-9][0-9]+?)(000)*$','$1ect-$2')" />
                  </externalid>
                </contentid>
                <input-template>
                  <externalid>p.TreeCategory</externalid>
                </input-template>
                <security-parent>
                  <externalid>
                    <xsl:value-of select="replace(broader/@qcode,'(subj):([0-9][0-9]+?)(000)*$','$1ect-$2')" />
                  </externalid>
                </security-parent>
              </metadata>
              <component group="polopoly.Content" name="name">
                <xsl:value-of select="./name[@xml:lang='en-GB']" />
              </component>
            </content>
          </xsl:if>
        </xsl:for-each>
      </batch>
    </batch>
  </xsl:template>
</xsl:stylesheet>
