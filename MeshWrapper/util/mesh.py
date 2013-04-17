import sys
import os

def dirCheck():
    docPath = os.path.abspath(sys.argv[0])
    docPath = os.path.dirname(docPath) + os.path.sep + "docs"
    if not os.path.exists(docPath):
        os.mkdir(docPath)

class mesh:
    from urllib.request import urlopen
    import xml.etree.ElementTree as ET
    
    baseURL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/"
    esearch = baseURL + "esearch.fcgi?db=mesh&term="
    efetch = baseURL + "efetch.fcgi?db=mesh&id="
    esummary = baseURL + "esummary.fcgi?db=mesh&id="

    def printHelp():
        """Print the input options."""
        print("Search: s [search terms]")
        print("Summary: sum [id]")
        print("Fetch: f [id]")
        print("q to quit")
    
    def search(query):
        """
        Saves the search result document and returns a list of ID's
        from a search using a query string.
        """
        # Construct URL
        query = query.strip()
        url = mesh.esearch + query
        url = url.replace(' ', '+')
        # Send request
        urlHandle = mesh.urlopen(url)
        # Read response, save locally
        docHandle = urlHandle.read()
        mesh.writeDoc(docHandle, "search_" + query + ".xml")
        # Find and return ID's
        xml = mesh.ET.XML(docHandle)
        ids = [x.text for x in xml.findall('.//Id')]
        return ids

    def summary(ID):
        """
        Write the summary document to disk and return the name and
        description from it.
        """
        # Construc URL
        url = mesh.esummary + str(ID)
        # Send request
        urlHandle = mesh.urlopen(url)
        # Read response
        docHandle = urlHandle.read()
        mesh.writeDoc(docHandle, "summary_" + str(ID) + ".xml")
        # Find and return name and description
        xml = mesh.ET.XML(docHandle)
        name, desc = "", ""
        itemNodes = [x for x in xml.findall('.//Item')]
        for item in itemNodes:
            if item.attrib['Name'] == "Name":
                name = item.text
            elif item.attrib['Name'] == "Description":
                desc = item.text
        return name, desc

    def fetch(ID):
        """Save full record XML object for ID to disk."""
        url = mesh.efetch + str(ID)
        urlHandle = mesh.urlopen(url)
        docHandle = urlHandle.read()
        mesh.writeDoc(docHandle, "fetch_" + str(ID) + ".txt")

    def writeDoc(doc, filename):
        """Write a document to disk."""
        filePtr = open("docs/"+filename, 'w+b')
        filePtr.write(doc)
        filePtr.close()

if __name__ == "__main__":
    mesh.printHelp()
    dirCheck()

    query = ""
    while query != 'q':
        query = input("--> ")
        querySplit= query.split(' ')
        if querySplit[0] == 's':
            terms = " ".join(querySplit[1:])
            print(mesh.search(terms))
        elif querySplit[0] == 'f':
            mesh.fetch(querySplit[1])
        elif querySplit[0] == 'sum':
            name, desc = mesh.summary(querySplit[1])
            print(name)
            print(desc)
        elif querySplit[0] == 'q':
            continue
        else:
            print("Didn't recognize command")
