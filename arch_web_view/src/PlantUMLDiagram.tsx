

interface PlantUMLDiagramProps {
    umlText: string;
}

export default function PlantUMLDiagram({umlText}: PlantUMLDiagramProps){

    const plantumlEncoder = require('plantuml-encoder');
    const encoded = plantumlEncoder.encode(umlText);
    const url = 'https://www.plantuml.com/plantuml/img/' + encoded;

    return (
        <div>
            <img src={url} alt="UML Diagram" />
        </div>
    );

}