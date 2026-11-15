import zipfile
import xml.etree.ElementTree as ET

def get_docx_text(path):
    try:
        with zipfile.ZipFile(path) as z:
            xml_content = z.read('word/document.xml')
            tree = ET.fromstring(xml_content)
            texts = []
            for node in tree.iter():
                if node.tag.endswith('t'):
                    if node.text:
                        texts.append(node.text)
                elif node.tag.endswith('p'):
                    texts.append('\n')
            return ''.join(texts)
    except Exception as e:
        return str(e)

if __name__ == '__main__':
    print(get_docx_text('chức-năng-từng-ae-movibox.docx'))
