import React, {useState} from "react";
import { Sankey, sankeyCenter, SankeyNode, SankeyLink } from "@visx/sankey";
import { Group } from "@visx/group";
import { ParentSize } from "@visx/responsive";
import { scaleOrdinal } from "d3-scale";
import {MedicinalUnitMode, MedicinalUnitModeUnits} from "../../types/MedicinalUnitMode";

type NodeDatum = { id: string; label: string };
type LinkDatum = { source: string; target: string; value: number };

interface SankeyChartProps {
    nodes: NodeDatum[];
    links: LinkDatum[];
    medicinalUnitMode: MedicinalUnitMode;
    height?: number;
}

export const SankeyChart: React.FC<SankeyChartProps> = ({
                                                            nodes,
                                                            links,
                                                            medicinalUnitMode,
                                                            height = 300
                                                        }) => {
    const color = scaleOrdinal<string, string>()
        .domain(nodes.map(n => n.label))
        .range(["#34558a", "#4f6da2", "#6c88b8", "#8aa2cb", "#abc", "#ddd"]);

    const unitWord = MedicinalUnitModeUnits[medicinalUnitMode];

    const graph = { nodes, links };

    return (
        <ParentSize debounceTime={10}>
            {({ width }) => {
                if (!width) return null;

                const margin = { top: 32, right: 48, bottom: 16, left: 32 };
                const innerW = width - margin.left - margin.right;
                const innerH = height - margin.top - margin.bottom;

                return (
                    <svg width={width} height={height}>
                        <Sankey<NodeDatum, LinkDatum>
                            root={graph}
                            size={[innerW, innerH]}
                            nodeAlign={sankeyCenter}
                            nodeWidth={20}
                            nodePadding={64}
                            nodeId={(d) => d.id}
                        >
                            {({ graph, createPath }) => (
                                <Group top={margin.top} left={margin.left}>
                                    {graph.links.map((link, i) => {
                                        const sourceLabel =
                                            typeof link.source === "object"
                                                ? (link.source as SankeyNode<NodeDatum, LinkDatum>).label
                                                : String(link.source);

                                        const targetLabel =
                                            typeof link.target === "object"
                                                ? (link.target as SankeyNode<NodeDatum, LinkDatum>).label
                                                : String(link.target);

                                        const pathD = createPath(link);
                                        if (!pathD) return null;

                                        const sourceNode = link.source as SankeyNode<NodeDatum, LinkDatum>;
                                        const targetNode = link.target as SankeyNode<NodeDatum, LinkDatum>;

                                        const linkWidth = link.width ?? 1;
                                        const textOffset = linkWidth < 20 ? 14 : 0; // Pokud je link moc úzký, posunout text nahoru

                                        const sourceX = (link.source as SankeyNode<NodeDatum, LinkDatum>).x1 ?? 0;
                                        const sourceY = (link.y0 ?? 0) + ((link.y1 ?? 0) - (link.y0 ?? 0)) / 2;
                                        const targetX = (link.target as SankeyNode<NodeDatum, LinkDatum>).x0 ?? 0;
                                        const targetY = (link.y0 ?? 0) + ((link.y1 ?? 0) - (link.y0 ?? 0)) / 2;

                                        const midX = (sourceX + targetX) / 2;
                                        const midY = (sourceY + targetY) / 2;

                                        // Vypočítat úhel
                                        const deltaX = targetX - sourceX;
                                        const deltaY = targetY - sourceY;
                                        const angleInRadians = Math.atan2(deltaY, deltaX);
                                        const angleInDegrees = angleInRadians * (180 / Math.PI);

                                        return (
                                            <g key={`link-${i}`}>
                                                <path
                                                    d={pathD}
                                                    fill="none"
                                                    stroke={color(targetLabel)}
                                                    strokeWidth={Math.max(1, linkWidth)}
                                                    strokeOpacity={0.35}
                                                >
                                                    <title>{`${sourceLabel} → ${targetLabel}: ${link.value} ${unitWord}`}</title>
                                                </path>

                                                {link.width && link.width > 64 && (
                                                    <text
                                                        x={midX}
                                                        y={midY}
                                                        dy="0.35em"
                                                        fontSize={10}
                                                        textAnchor="middle"
                                                        fill="#555"
                                                        pointerEvents="none"
                                                    >
                                                        {`${sourceLabel} → ${targetLabel}: ${link.value} ${unitWord}`}
                                                    </text>
                                                )}

                                            </g>
                                        );
                                    })}

                                    {graph.nodes.map((node, i) => {
                                        const x0 = node.x0 ?? 0;
                                        const x1 = node.x1 ?? 0;
                                        const y0 = node.y0 ?? 0;
                                        const y1 = node.y1 ?? 0;
                                        const label = node.label;

                                        return (
                                            <Group key={`node-${i}`}>
                                                <rect
                                                    x={x0}
                                                    y={y0}
                                                    width={x1 - x0}
                                                    height={y1 - y0}
                                                    fill={color(label)}
                                                    rx={4}
                                                />
                                                <text
                                                    x={(x0 + x1) / 2}
                                                    y={y0 - 8} // nad uzlem (trochu odsadíme nahoru)
                                                    textAnchor="middle"
                                                    fontSize={12}
                                                    fill="#333"
                                                >
                                                    {label}
                                                </text>
                                            </Group>
                                        );
                                    })}
                                </Group>
                            )}
                        </Sankey>
                    </svg>
                );
            }}
        </ParentSize>
    );
};
