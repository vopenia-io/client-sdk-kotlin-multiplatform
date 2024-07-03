import serverless from "serverless-http";
import express, { NextFunction } from "express";
import { AccessToken } from "livekit-server-sdk";

const app = express();

const bufferObject = (body: any) => {
  if (!body) return {};
  if (body instanceof Buffer) {
    try {
      return JSON.parse(body.toString());
    } catch (err) {
      //
    }
  }
  return {};
};

app.post("/", async (req, res, next) => {
  const body: any = bufferObject(req.body);
  const apiKey = body.api_key || process.env.API_KEY;
  const apiSecret = body.api_key || process.env.API_SECRET;
  const url = body.api_url || process.env.API_URL;
  
  if (!body.participant || typeof body.participant !== "string") {
    return res.status(404).json({ error: "invalid participant", body });
  }
  
  if (!body.room || typeof body.room !== "string") {
    return res.status(404).json({ error: "invalid room", body });
  }


  const at = new AccessToken(apiKey, apiSecret, {
    identity: body.participant,
  });

  at.addGrant({
    room: body.room,
    roomJoin: true,
    canPublish: true,
    canSubscribe: true
  });

  const token = await at.toJwt();

  return res.status(200).json({ token, url });
});

app.use((req, res, next) => {
  return res.status(404).json({
    error: "Not Found",
  });
});

exports.handler = serverless(app);
