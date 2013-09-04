<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="/">
    <batch xmlns="http://www.polopoly.com/polopoly/cm/xmlio">
      <content xmlns="http://www.polopoly.com/polopoly/cm/xmlio">
        <metadata>
          <contentid>
            <major>Department</major>
            <externalid>department.categorydimension.subject</externalid>
          </contentid>
        </metadata>
        <contentlist>
          <xsl:for-each select="//knowledgeItem/conceptSet/concept/conceptId[ends-with(@qcode, '000000')]/..">
            <entry mode="modify" withMetadata="false">
              <metadata>
                <referredContent>
                  <contentid>
                    <externalid>
                      <xsl:value-of select="replace(conceptId/@qcode,'(subj):([0-9][0-9]+?)(000)*$','$1ect-$2')" />
                    </externalid>
                  </contentid>
                </referredContent>
              </metadata>
            </entry>
          </xsl:for-each>
        </contentlist>
      </content>
      <xsl:for-each select="//knowledgeItem/conceptSet/concept/conceptId[ends-with(@qcode, '000000')]/..">
        <content xmlns="http://www.polopoly.com/polopoly/cm/xmlio">
          <metadata>
            <contentid>
              <major>Department</major>
              <externalid>
                <xsl:value-of select="replace(conceptId/@qcode,'(subj):([0-9][0-9]+?)(000)*$','$1ect-$2')" />
              </externalid>
            </contentid>
          </metadata>
          <contentlist>
            <xsl:variable name="qcode" select="conceptId/@qcode"></xsl:variable>
            <xsl:for-each select="//knowledgeItem/conceptSet/concept/conceptId[ends-with(@qcode, '000')]/..">
              <xsl:if test="./broader/@qcode = $qcode">
                <entry mode="modify" withMetadata="false">
                  <metadata>
                    <referredContent>
                      <contentid>
                        <externalid>
                          <xsl:value-of select="replace(conceptId/@qcode,'(subj):([0-9][0-9]+?)(000)*$','$1ect-$2')" />
                        </externalid>
                      </contentid>
                    </referredContent>
                  </metadata>
                </entry>
              </xsl:if>
            </xsl:for-each>
          </contentlist>
        </content>
      </xsl:for-each>
    </batch>
  </xsl:template>
</xsl:stylesheet>
